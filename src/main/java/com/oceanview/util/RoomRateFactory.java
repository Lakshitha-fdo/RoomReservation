package com.oceanview.util;

import com.oceanview.model.RoomType;

public final class RoomRateFactory {
    private RoomRateFactory() {
    }

    public static double getNightlyRate(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type is required.");
        }
        return roomType.nightlyRate();
    }
}
