package com.filterapp;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class GetInfo {
    public static void main(String[] args) {
        try {
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            OrtSession session = env.createSession("u2net_portrait.onnx", new OrtSession.SessionOptions());
            System.out.println("Input Info:");
            System.out.println(session.getInputInfo());
            System.out.println("Output Info:");
            System.out.println(session.getOutputInfo());
            session.close();
            env.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
