function assertThrows(block, message)
{
	if(typeof(block) != "function")
		throw new Error("Invalid parameter passed, cannot accept non-functions");

	try {
		block();
	}
	catch(e) {
		if( typeof(message) != "undefined")
			assertEquals(message, e.message);
		return;
	}

	fail("Should have thrown exception");
}

