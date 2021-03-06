/*
 *    Copyright  2019 Denis Kokorin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.kokorin.jaffree.ffmpeg;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeOutput extends TcpOutput<PipeOutput> implements Output {
    private final Consumer consumer;

    public PipeOutput(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected Consumer consumer() {
        return consumer;
    }

    public static PipeOutput withConsumer(Consumer consumer) {
        return new PipeOutput(consumer);
    }

    public static PipeOutput pumpTo(OutputStream destination) {
        return pumpTo(destination, 4 * 1024);
    }

    public static PipeOutput pumpTo(OutputStream destination, int bufferSize) {
        return new PipeOutput(new PipeConsumer(destination, bufferSize));
    }

    private static class PipeConsumer implements Consumer {
        private final OutputStream destination;
        private final int bufferSize;

        public PipeConsumer(OutputStream destination, int bufferSize) {
            this.destination = destination;
            this.bufferSize = bufferSize;
        }

        @Override
        public void consumeAndClose(InputStream source) {
            try (Closeable toClose = source) {
                IOUtil.copy(source, destination, bufferSize);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy data", e);
            }
        }
    }
}
