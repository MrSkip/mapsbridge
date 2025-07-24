const testDescription = pm.globals.get("testDescription");
const skipAddressValidation = pm.globals.get("skipAddressValidation") === "true";
const skipNameValidation = pm.globals.get("skipNameValidation") === "true";

// Helper function to safely parse coordinates
function safeParseFloat(value) {
    if (value === null || value === undefined) return null;
    const parsed = parseFloat(value);
    return isNaN(parsed) ? null : parsed;
}

// Basic response validation
pm.test(`[${testDescription}] Status code is 200`, function () {
    pm.response.to.have.status(200);
});

pm.test(`[${testDescription}] Response time is acceptable`, function () {
    pm.expect(pm.response.responseTime).to.be.below(10000);
});

pm.test(`[${testDescription}] Response has valid JSON structure`, function () {
    pm.response.to.be.json;

    const responseJson = pm.response.json();

    // Test main structure exists
    pm.expect(responseJson).to.have.property('coordinates');
    pm.expect(responseJson).to.have.property('name');
    pm.expect(responseJson).to.have.property('address');
    pm.expect(responseJson).to.have.property('links');
});

// Coordinates validation
pm.test(`[${testDescription}] Coordinates object validation`, function () {
    const responseJson = pm.response.json();
    const coordinates = responseJson.coordinates;

    // Test coordinates structure
    pm.expect(coordinates).to.have.property('lat');
    pm.expect(coordinates).to.have.property('lon');

    // Test coordinates values and types
    pm.expect(coordinates.lat).to.be.a('number');
    pm.expect(coordinates.lon).to.be.a('number');
    pm.expect(coordinates.lat).to.be.within(-90, 90);
    pm.expect(coordinates.lon).to.be.within(-180, 180);

    pm.expect(coordinates.valid).to.be.a('boolean');
});

pm.test(`[${testDescription}] Expected coordinate values match`, function () {
    const responseJson = pm.response.json();
    const coordinates = responseJson.coordinates;

    const expectedLat = safeParseFloat(pm.globals.get("expectedLat"));
    const expectedLon = safeParseFloat(pm.globals.get("expectedLon"));

    // Test coordinates if expected values are provided
    if (expectedLat !== null && expectedLon !== null) {
        pm.expect(coordinates.valid).to.be.true;
        pm.expect(coordinates.lat).to.be.closeTo(expectedLat, 0.0001,
            `Expected lat ${expectedLat}, got ${coordinates.lat}`);
        pm.expect(coordinates.lon).to.be.closeTo(expectedLon, 0.0001,
            `Expected lon ${expectedLon}, got ${coordinates.lon}`);

        console.log(`✅ Coordinates match: ${coordinates.lat}, ${coordinates.lon}`);
    }
});

// Name field validation
pm.test(`[${testDescription}] Name field validation`, function () {
    const responseJson = pm.response.json();
    const expectedName = pm.globals.get("expectedName");

    if (!skipNameValidation && expectedName && expectedName.trim() !== '') {
        if (responseJson.name !== null) {
            pm.expect(responseJson.name).to.be.a('string');
            // For flexible matching, check if expected name is contained in response
            const nameLower = responseJson.name.toLowerCase();
            const expectedLower = expectedName.toLowerCase();
            pm.expect(nameLower).to.include(expectedLower,
                `Expected name to contain "${expectedName}", got "${responseJson.name}"`);
        } else {
            // If name is null but we expected a value, log warning
            console.warn(`⚠️ Expected name "${expectedName}" but got null`);
        }
    } else {
        // Just validate type if name exists
        if (responseJson.name !== null) {
            pm.expect(responseJson.name).to.be.a('string');
        }
    }
});

// Address field validation
pm.test(`[${testDescription}] Address field validation`, function () {
    const responseJson = pm.response.json();
    const expectedAddress = pm.globals.get("expectedAddress");

    // Address should always be a string if coordinates are valid
    pm.expect(responseJson.address).to.be.a('string');
    pm.expect(responseJson.address.trim()).to.not.be.empty;

    // Test expected address content if provided and not skipped
    if (!skipAddressValidation && expectedAddress && expectedAddress.trim() !== '') {
        const addressLower = responseJson.address.toLowerCase();
        const expectedLower = expectedAddress.toLowerCase();

        pm.expect(addressLower).to.include(expectedLower,
            `Address "${responseJson.address}" should contain "${expectedAddress}"`);
    }
});

// Links validation
pm.test(`[${testDescription}] Links object validation`, function () {
    const responseJson = pm.response.json();
    const links = responseJson.links;

    if (responseJson.coordinates.valid) {
        // Test all expected link providers exist
        const expectedProviders = ['apple', 'komoot', 'bing', 'osm', 'waze', 'google'];

        expectedProviders.forEach(provider => {
            pm.expect(links).to.have.property(provider);
            pm.expect(links[provider]).to.be.a('string');
            pm.expect(links[provider]).to.match(/^https?:\/\/.+/,
                `${provider} link should be a valid URL: ${links[provider]}`);
        });

        console.log(`✅ All ${expectedProviders.length} link providers present`);
    }
});

pm.test(`[${testDescription}] Links contain correct coordinates`, function () {
    const responseJson = pm.response.json();
    const links = responseJson.links;
    const coordinates = responseJson.coordinates;

    if (coordinates.valid) {
        // Test that key links contain the extracted coordinates
        const latStr = coordinates.lat.toString();
        const lonStr = coordinates.lon.toString();

        // Check Google Maps link
        if (links.google) {
            const hasCoords = links.google.includes(latStr.substring(0, 8)) &&
                links.google.includes(lonStr.substring(0, 8));
            pm.expect(hasCoords, `Google link should contain coordinates: ${links.google}`).to.be.true;
        }

        // Check Apple Maps link
        if (links.apple) {
            const hasCoords = links.apple.includes(latStr.substring(0, 8)) &&
                links.apple.includes(lonStr.substring(0, 8));
            pm.expect(hasCoords, `Apple link should contain coordinates: ${links.apple}`).to.be.true
        }
    }
});

// Response data integrity
pm.test(`[${testDescription}] Response data integrity`, function () {
    const responseJson = pm.response.json();

    // Ensure response has expected number of top-level properties
    pm.expect(Object.keys(responseJson)).to.have.lengthOf(4); // coordinates, name, address, links

    if (responseJson.coordinates.valid) {
        pm.expect(responseJson.address).to.not.be.empty;
        pm.expect(Object.keys(responseJson.links)).to.have.lengthOf(6); // All 6 providers
    }
});

// Log test results
pm.test(`[${testDescription}] Log test results`, function () {
    const responseJson = pm.response.json();

    console.log(`📊 Test Results for: ${testDescription}`);
    console.log(`   ✓ Valid: ${responseJson.coordinates.valid}`);
    console.log(`   ✓ Coordinates: ${responseJson.coordinates.lat}, ${responseJson.coordinates.lon}`);
    console.log(`   ✓ Name: ${responseJson.name}`);
    console.log(`   ✓ Address: ${responseJson.address}`);
    console.log(`   ✓ Links count: ${Object.keys(responseJson.links).length}`);

    // Always pass this test
    pm.expect(true).to.be.true;
});

// Cleanup
pm.test(`[${testDescription}] Cleanup globals`, function () {
    // Clean up globals for next iteration
    pm.globals.unset("expectedLat");
    pm.globals.unset("expectedLon");
    pm.globals.unset("expectedAddress");
    pm.globals.unset("expectedName");
    pm.globals.unset("testDescription");
    pm.globals.unset("skipAddressValidation");
    pm.globals.unset("skipNameValidation");

    pm.expect(true).to.be.true;
});