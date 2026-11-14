package com.quilla.parqueo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Verifica que la aplicación completa arranque (todos los beans de Spring y la conexión a la base).
// IMPORTANTE: necesita PostgreSQL levantado en localhost:5433 → docker compose up -d db
// No hace falta levantar el backend: el test arranca su propia instancia de la aplicación.
// Los demás tests (lógica de cobro, conversión e importación) no necesitan la base de datos.
@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
