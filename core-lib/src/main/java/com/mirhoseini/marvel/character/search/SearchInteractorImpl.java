package com.mirhoseini.marvel.character.search;


import com.mirhoseini.marvel.domain.client.MarvelApi;
import com.mirhoseini.marvel.domain.model.CharactersResponse;
import com.mirhoseini.marvel.util.HashGenerator;
import com.mirhoseini.marvel.util.SchedulerProvider;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;

/**
 * Created by Mohsen on 20/10/2016.
 */

@Search
class SearchInteractorImpl implements SearchInteractor {

    private MarvelApi api;
    private SchedulerProvider scheduler;

    private ReplaySubject<CharactersResponse> characterSubject;
    private Subscription characterSubscription;

    @Inject
    SearchInteractorImpl(MarvelApi api, SchedulerProvider scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<CharactersResponse> loadCharacter(String query,
                                                        String privateKey,
                                                        String publicKey,
                                                        long timestamp) {
        if (characterSubscription == null || characterSubscription.isUnsubscribed()) {
            characterSubject = ReplaySubject.create();

            // generate hash using timestamp and API keys
            String hash = HashGenerator.generate(timestamp, privateKey, publicKey);

            characterSubscription = api.getCharacters(query, publicKey, hash, timestamp)
                    .subscribeOn(scheduler.backgroundThread())
                    .subscribe(characterSubject);
        }

        return characterSubject.asObservable();
    }


    @Override
    public void unbind() {
        if (characterSubscription != null && !characterSubscription.isUnsubscribed())
            characterSubscription.unsubscribe();
    }

}
