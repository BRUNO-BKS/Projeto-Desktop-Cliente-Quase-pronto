package com.buyo.adminfx.ui.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SplashController {
    @FXML private ProgressBar progressBar;
    @FXML private Text statusText;
    
    private Runnable onLoadingFinished;
    
    @FXML
    public void initialize() {
        // Animação de progresso
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.seconds(3), e -> {
                updateStatus("Carregando módulos...");
            }, new KeyValue(progressBar.progressProperty(), 0.5)),
            new KeyFrame(Duration.seconds(5), e -> {
                updateStatus("Conectando ao banco de dados...");
            }, new KeyValue(progressBar.progressProperty(), 0.8)),
            new KeyFrame(Duration.seconds(7), e -> {
                updateStatus("Pronto!");
                if (onLoadingFinished != null) {
                    Platform.runLater(() -> onLoadingFinished.run());
                }
            }, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }
    
    public void setOnLoadingFinished(Runnable onLoadingFinished) {
        this.onLoadingFinished = onLoadingFinished;
    }
    
    private void updateStatus(String status) {
        if (statusText != null) {
            statusText.setText(status);
        }
    }
}
