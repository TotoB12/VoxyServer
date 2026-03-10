package com.dripps.voxyserver.client;

import net.fabricmc.api.ClientModInitializer;

public class VoxyserverClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientLodReceiver.register();
    }
}
