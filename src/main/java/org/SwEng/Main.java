package org.SwEng;

import org.SwEng.headCoordinatorSystems.HeadCoordinator;

public class Main {
    public static void main(String[] args) {
        HeadCoordinator coordinator = new HeadCoordinator();
        coordinator.start();
    }
}