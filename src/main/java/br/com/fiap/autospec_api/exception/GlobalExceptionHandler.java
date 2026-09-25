package br.com.fiap.autospec_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Erro de validação dos dados enviados
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarErroValidacao(
            MethodArgumentNotValidException ex) {

        List<String> mensagens = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro ->
                        erro.getField()
                                + ": "
                                + erro.getDefaultMessage()
                )
                .toList();

        ErroResposta resposta = new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                mensagens
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }

    // 404 - Recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex) {

        ErroResposta resposta = new ErroResposta(
                HttpStatus.NOT_FOUND.value(),
                List.of(ex.getMessage())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(resposta);
    }

    // 401 - Credenciais inválidas
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException ex) {

        ErroResposta resposta = new ErroResposta(
                HttpStatus.UNAUTHORIZED.value(),
                List.of(ex.getMessage())
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(resposta);
    }
}