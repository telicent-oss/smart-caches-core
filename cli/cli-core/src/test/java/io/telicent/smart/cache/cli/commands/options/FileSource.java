/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.telicent.smart.cache.cli.commands.options;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.FileSourceOptions;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.Collections;

@Command(name = "file-source")
@NoArgsConstructor
public class FileSource extends SmartCacheCommand {

    @AirlineModule
    public FileSourceOptions<Bytes, Bytes> options = new FileSourceOptions<>();

    @Override
    public int run() {
        System.out.println("Use File Source? " + this.options.useFileSource());
        System.out.println("Using File Capture? " + StringUtils.isNotBlank(this.options.getCaptureDirectory()));

        if (this.options.useFileSource()) {
            EventSource<Bytes, Bytes> source =
                    this.options.getFileSource(new BytesDeserializer(), new BytesDeserializer());
            System.out.println("File Source opened OK");
            System.out.println(source.toString());
            source.close();
        }
        if (StringUtils.isNotBlank(this.options.getCaptureDirectory())) {
            try (Sink<Event<Bytes, Bytes>> sink =
                         this.options.getCaptureSink(new BytesSerializer(), new BytesSerializer(),
                                                     Collections.emptyList(), Collections.emptyList())) {
                System.out.println(sink.toString());
                System.out.println("File Capture opened OK");
            }
        }

        return 0;
    }
}
