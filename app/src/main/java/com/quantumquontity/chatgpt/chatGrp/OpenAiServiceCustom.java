package com.quantumquontity.chatgpt.chatGrp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.SSE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class OpenAiServiceCustom extends OpenAiService {

    static ObjectMapper mapper = defaultObjectMapper();

    private OpenAiApi api;

    public OpenAiServiceCustom(OpenAiApi api, ExecutorService executorService) {
        super(api, executorService);
        this.api = api;
    }

    public void executeChatCompletion(ChatCompletionRequest request, Consumer<ChatCompletionChunk> consumer) throws IOException {
        request.setStream(true);
        executeChatCompletion(api.createChatCompletionStream(request), consumer);
    }

    public void executeChatCompletion(Call<ResponseBody> apiCall, Consumer<ChatCompletionChunk> consumer) throws IOException {
        Response<ResponseBody> response = apiCall.execute();
        if (response.isSuccessful()) {
            parseSse(response, consumer);
        } else {
            throw new IOException("Request error: " + response.code() + " " + response.message());
        }
    }

    public void parseSse(Response<ResponseBody> response, Consumer<ChatCompletionChunk> consumer) {
        BufferedReader reader = null;
        try {
            InputStream in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            SSE sse = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    sse = new SSE(data);
                    if (sse.isDone()) {
                        break;
                    }
                    try {
                        ChatCompletionChunk chatCompletionChunk = mapper.readValue(sse.getData(), ChatCompletionChunk.class);
                        consumer.accept(chatCompletionChunk);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else if (line.isEmpty() && sse != null) {
                    if (sse.isDone()) {
                        break;
                    }
                } else {
                    throw new RuntimeException("Oups...");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception parseSse");
        }
    }
}