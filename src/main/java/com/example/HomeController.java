package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
public String home() {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Java DevSecOps CI/CD</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', system-ui, sans-serif;
                        background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
                        color: #e2e8f0;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 2rem;
                    }
                    .card {
                        background: rgba(30, 41, 59, 0.7);
                        backdrop-filter: blur(10px);
                        border: 1px solid rgba(148, 163, 184, 0.15);
                        border-radius: 16px;
                        padding: 3rem;
                        max-width: 640px;
                        width: 100%;
                        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
                    }
                    .badge {
                        display: inline-block;
                        background: rgba(34, 197, 94, 0.15);
                        color: #4ade80;
                        border: 1px solid rgba(74, 222, 128, 0.4);
                        padding: 0.35rem 0.9rem;
                        border-radius: 999px;
                        font-size: 0.8rem;
                        font-weight: 600;
                        letter-spacing: 0.03em;
                        margin-bottom: 1.5rem;
                    }
                    h1 {
                        font-size: 2rem;
                        background: linear-gradient(90deg, #60a5fa, #34d399);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        margin-bottom: 0.5rem;
                    }
                    .subtitle {
                        color: #94a3b8;
                        font-size: 1rem;
                        margin-bottom: 2rem;
                    }
                    .status {
                        display: flex;
                        align-items: center;
                        gap: 0.6rem;
                        background: rgba(15, 23, 42, 0.6);
                        border-left: 3px solid #34d399;
                        padding: 0.9rem 1.2rem;
                        border-radius: 8px;
                        margin-bottom: 2rem;
                        font-size: 0.95rem;
                    }
                    .status .dot {
                        width: 10px;
                        height: 10px;
                        border-radius: 50%;
                        background: #34d399;
                        box-shadow: 0 0 8px #34d399;
                        flex-shrink: 0;
                    }
                    .pipeline {
                        display: flex;
                        justify-content: space-between;
                        gap: 0.5rem;
                    }
                    .stage {
                        flex: 1;
                        text-align: center;
                    }
                    .stage .check {
                        width: 32px;
                        height: 32px;
                        margin: 0 auto 0.5rem;
                        border-radius: 50%;
                        background: rgba(34, 197, 94, 0.15);
                        border: 1px solid rgba(74, 222, 128, 0.4);
                        color: #4ade80;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 0.9rem;
                        font-weight: bold;
                    }
                    .stage span {
                        font-size: 0.7rem;
                        color: #94a3b8;
                        display: block;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="badge">● ALL SYSTEMS OPERATIONAL</div>
                    <h1>Java DevSecOps CI/CD</h1>
                    <p class="subtitle">Secure delivery pipeline for a Java application on Amazon EKS</p>

                    <div class="status">
                        <div class="dot"></div>
                        Application is running successfully — all security scans passed
                    </div>

                    <div class="pipeline">
                        <div class="stage"><div class="check">&#10003;</div><span>Secrets</span></div>
                        <div class="stage"><div class="check">&#10003;</div><span>Lint</span></div>
                        <div class="stage"><div class="check">&#10003;</div><span>SAST</span></div>
                        <div class="stage"><div class="check">&#10003;</div><span>Image Scan</span></div>
                        <div class="stage"><div class="check">&#10003;</div><span>DAST</span></div>
                    </div>
                </div>
            </body>
            </html>
            """;
}

    @GetMapping("/health")
    public String health() {
        return "Application is Healthy";
    }

}