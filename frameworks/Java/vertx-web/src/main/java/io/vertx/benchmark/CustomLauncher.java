package io.vertx.benchmark;

import io.vertx.core.Launcher;

public class CustomLauncher extends Launcher {

    public static void main(String[] args) {
        new CustomLauncher().dispatch(args);
    }
}
