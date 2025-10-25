package com.reservas.api.controller;

import com.reservas.api.entities.dto.LoginRequest;
import com.reservas.api.entities.dto.LoginResponse;
import com.reservas.api.entities.dto.UserRegisterRequest;
import com.reservas.api.entities.dto.UserResponse;
import com.reservas.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints para Autenticação (Registro e Login)")
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "Registrar Novo Usuário", description = "Cria uma nova conta de usuário (Cliente) no sistema. Por padrão, a role será USER.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
			             content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos na requisição (ex: email já existe, CPF já existe, campos obrigatórios faltando)",
			             content = @Content)
	})
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest registerRequest) {
		UserResponse userCreated = authService.register(registerRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
	}

	@Operation(summary = "Realizar Login", description = "Autentica um usuário com email e senha e retorna um token JWT para acesso aos endpoints protegidos.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login bem-sucedido, token JWT retornado",
			             content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Requisição inválida (campos faltando)", content = @Content),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas (email ou senha incorretos)", content = @Content)
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.login(loginRequest);
		return ResponseEntity.ok(loginResponse);
	}
}
