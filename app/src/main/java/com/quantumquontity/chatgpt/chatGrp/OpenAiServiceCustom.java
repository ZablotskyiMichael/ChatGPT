package com.quantumquontity.chatgpt.chatGrp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.ResponseBodyCallback;
import com.theokanning.openai.service.SSE;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class OpenAiServiceCustom extends OpenAiService {

    static ObjectMapper mapper = defaultObjectMapper();

    private OpenAiApi api;
    public OpenAiServiceCustom(String token) {
        super(token);
    }

    public OpenAiServiceCustom(String token, Duration timeout) {
        super(token, timeout);
    }

    public OpenAiServiceCustom(OpenAiApi api) {
        super(api);
        this.api = api;
    }

    public OpenAiServiceCustom(OpenAiApi api, ExecutorService executorService) {
        super(api, executorService);
        this.api = api;
    }

    @Override
    public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request) {
        request.setStream(true);
        return stream1(api.createChatCompletionStream(request), ChatCompletionChunk.class);
    }

    public static <T> Flowable<T> stream1(Call<ResponseBody> apiCall, Class<T> cl) {
        System.out.println("stream1");
        return stream2(apiCall).map(sse -> mapper.readValue(sse.getData(), cl));
    }

    public static Flowable<SSE> stream2(Call<ResponseBody> apiCall) {
        System.out.println("stream2");
        return stream3(apiCall, false);
    }

    public static Flowable<SSE> stream3(Call<ResponseBody> apiCall, boolean emitDone) {
        System.out.println("stream3");
        return Flowable.create(emitter -> {
            System.out.println("before enqueue");
            apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone));
            System.out.println("after enqueue");
        }, BackpressureStrategy.BUFFER);
    }


    /*public void doRequest(ChatCompletionRequest request, Consumer<ChatCompletionChunk> consumer) {
        Call<ResponseBody> call = api.createChatCompletionStream(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // обработка успешного ответа
                if (response.isSuccessful()) {
                    // получите тело ответа и обработайте его
                    ResponseBody body = response.body();
                    // обработайте тело ответа в фоновом потоке
                    // ...
                    try {
                        ChatCompletionChunk chatCompletionChunk = mapper.readValue(body.toString(), ChatCompletionChunk.class);
                        consumer.accept(chatCompletionChunk);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // обработка неудачного ответа
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // обработка ошибки
            }
        });
    }*/
}
