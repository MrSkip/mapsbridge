package com.example.mapsbridge.dto.response;

import com.example.mapsbridge.dto.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response model for the map link conversion API.
 * Contains the extracted location information and links to various map providers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebConvertResponse extends BaseConvertResponse {

    private Coordinate coordinates;
    private String name;
    private String address;

}
