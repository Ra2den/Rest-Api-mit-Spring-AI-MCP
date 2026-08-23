package com.example.restapimitspringaimcp.model;

import java.util.List;

public record FullRoomInfo(
        RoomDetail details,
        List<RoomAssignment> timetable
) {}