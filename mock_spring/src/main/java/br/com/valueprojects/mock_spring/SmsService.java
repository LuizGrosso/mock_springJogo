package br.com.valueprojects.mock_spring;

public interface SmsService {
    void enviarSms(String numero, String mensagem);
}