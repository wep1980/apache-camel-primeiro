package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
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
						.split()// Dividindo o conteudo do arquivo por item
						   .xpath("/pedido/itens/item")
						   //.log("${body}") // Mostrando o ID e o corpo de cada mensagem atraves do log
						.filter()
						   //.xpath("/pedido/itens/item/formato[text()='EBOOK']") // Acessando um elemento dentro do arquivo XML, para isso e inserido o caminho ate chegar no elemento formato, e trazer apenas os elementos que sejam do formato EBOOK
						   .xpath("/item/formato[text()='EBOOK']") // Acessando um elemento dentro do arquivo XML, OBS : APOS O SPLIT ACIMA PARA DIVIDIR O ARQUIVO POR ITEM O CAMINHO ATE O FORMATO FICOU MENOR
						//.log("${id} - ${body}") // Mostrando o ID e o corpo de cada mensagem
						//.log("${id}") // Mostrando o ID e o corpo de cada mensagem
						.marshal().xmljson() // Transformando de XML em JSON
						.log("${id} - ${body}") // Imprindo a mensagem transformada no console
						//.setHeader("CamelFileName", constant("pedido.json")) // Alterando o tipo de arquivo de XML para JSON, so que desta forma transformou e enviou apenas 1 arquivo
						//.setHeader("CamelFileName", simple("${file:name.noext}.json")) // Alterando o tipo de XML para JSON de todos os arquvios
						.setHeader(Exchange.HTTP_METHOD, HttpMethods.GET) // Faz uma requisicao a um metodo GET no servico do endereço abaixo
						.setHeader(Exchange.HTTP_QUERY, constant("ebookId=ARQ&pedidoId=2451256&clienteId=edgar.b@abc.com"))// enviando os parametros do metodo GET
						//.to("file:saida");
				        .to("http4://localhost:8080/webservices/ebook/item");

			}
		});

		context.start();
		Thread.sleep(10000); // 10 segundos de pausa
		context.stop();

	}	
}
