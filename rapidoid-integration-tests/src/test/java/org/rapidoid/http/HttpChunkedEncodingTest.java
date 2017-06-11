package org.rapidoid.http;

/*
 * #%L
 * rapidoid-integration-tests
 * %%
 * Copyright (C) 2014 - 2017 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.log.Log;
import org.rapidoid.setup.On;
import org.rapidoid.u.U;

import java.io.OutputStream;

@Authors("Nikolche Mihajlovski")
@Since("5.4.0")
public class HttpChunkedEncodingTest extends IsolatedIntegrationTest {

	@Test(timeout = 20000)
	public void testChunkedEncoding() {
		Log.debugging();

		On.req(req -> {
			OutputStream out = req.out();

			out.write("ab".getBytes());
			out.write("c".getBytes());
			out.flush();

			out.write("d".getBytes());

			out.close();

			return req;
		});

		getReq("/");

		Self.get("/").expect("abcd").execute();
		Self.get("/").expect("abcd").benchmark(1, 100, 10000);
		Self.post("/").expect("abcd").benchmark(1, 100, 10000);
	}

	@Test(timeout = 20000)
	public void testChunkedEncodingAsync() {
		Log.debugging();

		On.req(req -> {
			U.must(!req.isAsync());
			req.async();
			U.must(req.isAsync());

			OutputStream out = req.out();

			async(() -> {
				out.write("ab".getBytes());
				out.flush();

				async(() -> {
					out.write("c".getBytes());
					out.flush();

					async(() -> {
						out.write("d".getBytes());
						out.close();
						req.done();
					});
				});
			});

			return req;
		});

		getReq("/");

		Self.get("/").expect("abcd").execute();
		Self.get("/").expect("abcd").benchmark(1, 100, 10000);
		Self.post("/").expect("abcd").benchmark(1, 100, 10000);
	}

}
