package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();

		context.addRoutes(new RouteBuilder() { // Adicionando uma rota
			@Override
			public void configure() throws Exception {

				/**
				 * Lendo as mensagens de um arquivo, mas poderia ser de uma fila JMS,
				 * delay=5s -> le o arquivo de 5 em 5 segundos
				 * noop=true -> Comando para nao apagar os arquivos da pasta pedidos ao transferir para a pasta saida
				 */
				from("file:pedidos?delay=5s&noop=true")
						//.log("${id} - ${body}") // Mostrando o ID e o corpo de cada mensagem
						.log("${id}") // Mostrando o ID e o corpo de cada mensagem
						.marshal().xmljson() // Transformando de XML em JSON
						.log("${body}") // Imprindo a mensagem transformada no console
						//.setHeader("CamelFileName", constant("pedido.json")) // Alterando o tipo de arquivo de XML para JSON, so que desta forma transformou e enviou apenas 1 arquivo
						.setHeader("CamelFileName", simple("${file:name.noext}.json")) // Alterando o tipo de XML para JSON de todos os arquvios
						.to("file:saida");

			}
		});

		context.start();
		Thread.sleep(10000); // 10 segundos de pausa
		context.stop();

	}	
}
