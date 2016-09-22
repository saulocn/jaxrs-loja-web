package br.com.alura.loja;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.alura.loja.modelo.Carrinho;
import br.com.alura.loja.modelo.Produto;
import br.com.alura.loja.modelo.Projeto;

public class ClienteTest {

	private HttpServer server;
	private String urlLocal = "http://localhost:8087";
	private Client client;

	@Before
	public void before() {
		System.out.println("Inicializando Servidor ...");
		ClientConfig config = new ClientConfig();
		config.register(new LoggingFilter());
		this.client = ClientBuilder.newClient(config);
		server = Servidor.inicializaServidor();
		System.out.println("Servidor Iniciado!");

	}

	@After
	public void after() {
		System.out.println("Parando Servidor ...");
		Servidor.mataServidor(server);
		System.out.println("Servidor Parado!");
	}

	@Test
	public void testaQueAConexaoComOServidorFunciona() {
		WebTarget target = client.target("http://www.mocky.io");
		String conteudo = target.path("/v2/52aaf5deee7ba8c70329fb7d").request().get(String.class);
		Assert.assertTrue(conteudo.contains("Rua Vergueiro 3185"));
	}

	@Test
	public void testaQueBuscarUmCarrinhoTrazOCarrinhoEsperado() {

		WebTarget target = client.target(urlLocal);
		Carrinho carrinho = target.path("/carrinhos/1").request().get(Carrinho.class);
		System.out.println(carrinho);

		Assert.assertEquals("Rua Vergueiro 3185, 8 andar", carrinho.getRua());
	}

	@Test
	public void testaQueBuscarUmProjetoTrazOProjetoEsperado() {

		WebTarget target = client.target(urlLocal);
		Projeto projeto = target.path("/projetos/1").request().get(Projeto.class);
		System.out.println(projeto);
		Assert.assertEquals("Minha loja", projeto.getNome());
	}

	@Test
	public void testaCadastrarNovoCarrinho() {

		WebTarget target = client.target(urlLocal);

		Carrinho carrinho = new Carrinho();
		carrinho.adiciona(new Produto(314L, "Tablet", 999, 1));
		carrinho.setRua("Rua Vergueiro");
		carrinho.setCidade("Sao Paulo");

		Entity<Carrinho> entity = Entity.entity(carrinho, MediaType.APPLICATION_XML);

		Response response = target.path("/carrinhos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		String location = response.getHeaderString("Location");
		Carrinho carrinhoRetorno = client.target(location).request().get(Carrinho.class);
		Assert.assertEquals("Tablet", carrinhoRetorno.getProdutos().get(0).getNome());
	}

	@Test
	public void testaCadastrarNovoProjeto() {

		WebTarget target = client.target(urlLocal);

		Projeto projeto = new Projeto();
		projeto.setNome("Teste");
		projeto.setId(4);
		projeto.setAnoDeInicio(2011);

		String xml = projeto.toXML();

		Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);

		Response response = target.path("/projetos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		String location = response.getHeaderString("Location");
		String conteudo = client.target(location).request().get(String.class);
		Assert.assertTrue(conteudo.contains("Teste"));
	}
}
