package com.example.mapsbridge.dto.response.shortcut;

import com.example.mapsbridge.dto.response.BaseConvertResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ShortcutBaseResponse extends BaseConvertResponse {
    private boolean success;
}
