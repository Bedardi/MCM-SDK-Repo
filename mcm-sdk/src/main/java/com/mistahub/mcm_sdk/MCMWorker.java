package com.mistahub.mcm_sdk;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MCMWorker extends Worker {

    public MCMWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueueTokenSync(Context context, String token, String appId, String url) {
        Data inputData = new Data.Builder()
                .putString("token", token)
                .putString("appId", appId)
                .putString("url", url) // URL yahan worker ko pass ho raha hai
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MCMWorker.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    @NonNull
    @Override
    public Result doWork() {
        String token = getInputData().getString("token");
        String appId = getInputData().getString("appId");
        
        // YAHAN WORKER US URL KO NIKAL RAHA HAI JO CLIENT NE PASS KIYA THA
        String adminBridgeUrl = getInputData().getString("url"); 

        if (adminBridgeUrl == null || adminBridgeUrl.isEmpty()) {
            return Result.failure();
        }

        OkHttpClient client = new OkHttpClient();
        String json = "{\"appId\":\"" + appId + "\", \"deviceToken\":\"" + token + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(adminBridgeUrl) // HTTP POST Request directly to Dashboard Broker
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() ? Result.success() : Result.retry();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
