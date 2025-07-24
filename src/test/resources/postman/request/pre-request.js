// Get data from the current iteration
const testData = pm.iterationData.toObject();

// Validate that we have required data
if (!testData.input) {
    throw new Error("Input URL is required in data file");
}

// Set the request body with data from JSON file
pm.request.body = {
    mode: 'raw',
    raw: JSON.stringify({
        "input": testData.input
    }),
    options: {
        raw: {
            language: 'json'
        }
    }
};

// Store test data for validation
pm.globals.set("expectedLat", testData.expected?.coordinates?.lat || null);
pm.globals.set("expectedLon", testData.expected?.coordinates?.lon || null);
pm.globals.set("expectedAddress", testData.expected?.address || null);
pm.globals.set("expectedName", testData.expected?.name || null);
pm.globals.set("testDescription", testData.testConfig?.description || "Unknown Test");
pm.globals.set("skipAddressValidation", testData.testConfig?.skipAddressValidation || false);
pm.globals.set("skipNameValidation", testData.testConfig?.skipNameValidation || false);

// Set request headers
pm.request.headers.add({
    key: 'Content-Type',
    value: 'application/json'
});

console.log(`🧪 Running test: ${testData.testConfig?.description}`);
console.log(`🔗 Input URL: ${testData.input}`);
console.log(`📍 Expected coordinates: ${testData.expected?.coordinates?.lat}, ${testData.expected?.coordinates?.lon}`);