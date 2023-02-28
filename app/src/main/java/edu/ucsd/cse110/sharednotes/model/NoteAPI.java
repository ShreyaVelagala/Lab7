package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    /*
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public String GetNote(String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null) // body is null because not putting anything into the server
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            return body;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public void PutNote(Note note) {
        String jsonStr = new Gson().toJson(Map.of("content", note.content,
                                            "updated_at", note.updatedAt));
        RequestBody rqb = RequestBody.create(jsonStr, JSON);
        Thread networkThread = new Thread(()-> {
            // URLs cannot contain spaces, so we replace them with %20.
            var title = note.title;
            title = title.replace(" ", "%20");
            var request = new Request.Builder()
                    .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                    .method("PUT", rqb)
                    .build();

            try (var response = client.newCall(request).execute()) {
                assert response.body() != null;
                var body = response.body().string();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        networkThread.start();

    }
}
