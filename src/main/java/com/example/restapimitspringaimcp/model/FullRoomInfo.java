package com.example.restapimitspringaimcp.model;

import java.util.List;
import java.util.Map;

public record FullRoomInfo(
        RoomDetail details,
        Map<String, List<FormattedAssignment>> timetable
) {}