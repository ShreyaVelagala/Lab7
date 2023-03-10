package edu.ucsd.cse110.sharednotes.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class NoteRepository {
    private final NoteDao dao;
    NoteAPI api = new NoteAPI();

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     *
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (ourNote == null || ourNote.updatedAt < theirNote.updatedAt) {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();

    }

    public void upsertLocal(Note note) {
        note.updatedAt = System.currentTimeMillis();
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        // ScheduledFuture result of scheduling a task with a Scheduled Executor Service
        ScheduledFuture<?> clockFuture;
        // LiveData variable which contains the latest note content
        MutableLiveData<Note> updatedNote = new MutableLiveData<Note>();

        //MutableLiveData<Note> note = (MutableLiveData<Note>) getLocal(title); // returns the note

        var executor = Executors.newSingleThreadScheduledExecutor();
        clockFuture = executor.scheduleAtFixedRate(() -> {
            // fetching new note content from server
            String content = api.GetNote(title);
            updatedNote.postValue(new Note(title, content));
        }, 0, 3000, TimeUnit.MILLISECONDS);
        return updatedNote;
        /*
        NoteAPI api = new NoteAPI();
        Note n = note.getValue(); // getting the note object from LiveData<Note>
            clockFuture = executor.scheduleAtFixedRate(() -> {
            String content = api.GetNote(title);
            n.content = content;
            dao.upsert(n);
        }, 0, 3000, TimeUnit.MILLISECONDS);

        throw new UnsupportedOperationException("Not implemented yet");
         */
    }

    public void upsertRemote(Note note) {
        //String title = note.title;
        if(!dao.exists(note.title)) {
            note.updatedAt = System.currentTimeMillis();
        }
        api.PutNote(note);
        // TODO: Implement upsertRemote
        //throw new UnsupportedOperationException("Not implemented yet");
    }
}
