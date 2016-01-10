/*
 * Copyright 2016 Clerk.
 *
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
 */
package com.ideastormsoftware.presmedia.sources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.avcodec.AVCodec;
import org.bytedeco.javacpp.avcodec.AVCodecContext;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avcodec.AVPicture;
import static org.bytedeco.javacpp.avcodec.av_free_packet;
import static org.bytedeco.javacpp.avcodec.avcodec_close;
import static org.bytedeco.javacpp.avcodec.avcodec_decode_video2;
import static org.bytedeco.javacpp.avcodec.avcodec_find_decoder;
import static org.bytedeco.javacpp.avcodec.avcodec_open2;
import static org.bytedeco.javacpp.avcodec.avpicture_fill;
import static org.bytedeco.javacpp.avcodec.avpicture_get_size;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import static org.bytedeco.javacpp.avformat.av_dump_format;
import static org.bytedeco.javacpp.avformat.av_read_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.avformat_close_input;
import static org.bytedeco.javacpp.avformat.avformat_find_stream_info;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import org.bytedeco.javacpp.avutil.AVDictionary;
import org.bytedeco.javacpp.avutil.AVFrame;
import static org.bytedeco.javacpp.avutil.AVMEDIA_TYPE_VIDEO;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB24;
import static org.bytedeco.javacpp.avutil.av_frame_alloc;
import static org.bytedeco.javacpp.avutil.av_free;
import static org.bytedeco.javacpp.avutil.av_malloc;
import static org.bytedeco.javacpp.swscale.SWS_BILINEAR;
import org.bytedeco.javacpp.swscale.SwsContext;
import static org.bytedeco.javacpp.swscale.sws_getContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

/**
 *
 * @author Clerk
 */
public class FFmpegTest {

    static void SaveFrame(AVFrame pFrame, int width, int height, int iFrame)
            throws IOException {
        // Open file
        OutputStream stream = new FileOutputStream("frame" + iFrame + ".ppm");

        // Write header
        stream.write(("P6\n" + width + " " + height + "\n255\n").getBytes());

        // Write pixel data
        BytePointer data = pFrame.data(0);
        byte[] bytes = new byte[width * 3];
        int l = pFrame.linesize(0);
        for (int y = 0; y < height; y++) {
            data.position(y * l).get(bytes);
            stream.write(bytes);
        }

        // Close file
        stream.close();
    }

    public static void main(String[] args) throws IOException {
        AVFormatContext pFormatCtx = new AVFormatContext(null);
        int i, videoStream;
        AVCodecContext pCodecCtx = null;
        AVCodec pCodec = null;
        AVFrame pFrame = null;
        AVFrame pFrameRGB = null;
        AVPacket packet = new AVPacket();
        int[] frameFinished = new int[1];
        int numBytes;
        BytePointer buffer = null;

        AVDictionary optionsDict = null;
        SwsContext sws_ctx = null;

        if (args.length < 1) {
            System.out.println("Please provide a movie file");
            System.exit(-1);
        }
        // Register all formats and codecs
        av_register_all();

        // Open video file
        if (avformat_open_input(pFormatCtx, args[0], null, null) != 0) {
            System.exit(-1); // Couldn't open file
        }

        // Retrieve stream information
        if (avformat_find_stream_info(pFormatCtx, (PointerPointer) null) < 0) {
            System.exit(-1); // Couldn't find stream information
        }

        // Dump information about file onto standard error
        av_dump_format(pFormatCtx, 0, args[0], 0);

        // Find the first video stream
        videoStream = -1;
        for (i = 0; i < pFormatCtx.nb_streams(); i++) {
            if (pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO) {
                videoStream = i;
                break;
            }
        }
        if (videoStream == -1) {
            System.exit(-1); // Didn't find a video stream
        }

        // Get a pointer to the codec context for the video stream
        pCodecCtx = pFormatCtx.streams(videoStream).codec();

        // Find the decoder for the video stream
        pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
        if (pCodec == null) {
            System.err.println("Unsupported codec!");
            System.exit(-1); // Codec not found
        }
        // Open codec
        if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
            System.exit(-1); // Could not open codec
        }

        // Allocate video frame
        pFrame = av_frame_alloc();

        // Allocate an AVFrame structure
        pFrameRGB = av_frame_alloc();
        if (pFrameRGB == null) {
            System.exit(-1);
        }

        // Determine required buffer size and allocate buffer
        numBytes = avpicture_get_size(AV_PIX_FMT_RGB24,
                pCodecCtx.width(), pCodecCtx.height());
        buffer = new BytePointer(av_malloc(numBytes));

        sws_ctx = sws_getContext(pCodecCtx.width(), pCodecCtx.height(),
                pCodecCtx.pix_fmt(), pCodecCtx.width(), pCodecCtx.height(),
                AV_PIX_FMT_RGB24, SWS_BILINEAR, null, null, (DoublePointer) null);

        // Assign appropriate parts of buffer to image planes in pFrameRGB
        // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
        // of AVPicture
        avpicture_fill(new AVPicture(pFrameRGB), buffer, AV_PIX_FMT_RGB24,
                pCodecCtx.width(), pCodecCtx.height());

        // Read frames and save first five frames to disk
        i = 0;
        while (av_read_frame(pFormatCtx, packet) >= 0) {
            // Is this a packet from the video stream?
            if (packet.stream_index() == videoStream) {
                // Decode video frame
                avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);

                // Did we get a video frame?
                if (frameFinished[0] != 0) {
                    // Convert the image from its native format to RGB
                    sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0,
                            pCodecCtx.height(), pFrameRGB.data(), pFrameRGB.linesize());

                    // Save the frame to disk
                    if (++i <= 5) {
                        SaveFrame(pFrameRGB, pCodecCtx.width(), pCodecCtx.height(), i);
                    }
                }
            }

            // Free the packet that was allocated by av_read_frame
            av_free_packet(packet);
        }

        // Free the RGB image
        av_free(buffer);
        av_free(pFrameRGB);

        // Free the YUV frame
        av_free(pFrame);

        // Close the codec
        avcodec_close(pCodecCtx);

        // Close the video file
        avformat_close_input(pFormatCtx);

        System.exit(0);
    }
}
