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
						.setProperty("pedidoId", xpath("/pedido/id/text()")) // Definindo variavel da rota, pedidoId(setando uma propriedade)
						.setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()")) // Definindo variavel da rota, clienteId(setando uma propriedade)
						.split()// Dividindo o conteudo do arquivo por item
						   .xpath("/pedido/itens/item")
						.filter()
						   .xpath("/item/formato[text()='EBOOK']") // Acessando um elemento dentro do arquivo XML, OBS : APOS O SPLIT ACIMA PARA DIVIDIR O ARQUIVO POR ITEM O CAMINHO ATE O FORMATO FICOU MENOR
						.setProperty("ebookId", xpath("/item/livro/codigo/text()")) // Definindo variavel da rota, clienteId(setando uma propriedade)
						.marshal().xmljson() // Transformando de XML em JSON
						.log("${id} - ${body}") // Imprindo a mensagem transformada no console
						.setHeader(Exchange.HTTP_METHOD, HttpMethods.GET) // Faz uma requisicao a um metodo GET no servico do endereço abaixo
						.setHeader(Exchange.HTTP_QUERY, simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}"))// enviando os parametros do metodo GET
				        .to("http4://localhost:8080/webservices/ebook/item");

			}
		});

		context.start();
		Thread.sleep(10000); // 10 segundos de pausa
		context.stop();

	}	
}
