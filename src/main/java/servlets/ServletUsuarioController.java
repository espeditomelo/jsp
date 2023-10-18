package servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.DAOUsuarioRepository;
import model.ModelLogin;



@MultipartConfig
@WebServlet(urlPatterns = {"/ServletUsuarioController"})
public class ServletUsuarioController extends ServletGenericUtil {
	private static final long serialVersionUID = 1L;

	private DAOUsuarioRepository daoUsuarioRepository = new DAOUsuarioRepository();

	public ServletUsuarioController() {
		super();
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		try {
			String acao = request.getParameter("acao");		
			
			if (acao != null && !acao.isEmpty() && acao.equalsIgnoreCase("deletar")) {
				String id = request.getParameter("id");
				daoUsuarioRepository.deletarUsuario(id);
				
				List<ModelLogin> listaUsuarios = daoUsuarioRepository.listarUsuarios(super.getUsuarioLogado(request));
				request.setAttribute("listaUsuarios", listaUsuarios);				
				
				request.setAttribute("msg", "Usuario execluido com sucesso.");
				request.getRequestDispatcher("principal/usuario.jsp").forward(request, response);
				
			} else if (acao != null && !acao.isEmpty() && acao.equalsIgnoreCase("deletarAjax")) {
				String id = request.getParameter("id");
				daoUsuarioRepository.deletarUsuario(id);
				response.getWriter().write("Usuario execluido com sucesso.");
								
			} else if (acao != null && !acao.isEmpty() && acao.equalsIgnoreCase("PesquisarUsuarioAjax")) {
				String nomePesquisa = request.getParameter("nomePesquisa");
				List<ModelLogin> lista = daoUsuarioRepository.pesquisarUsuario(nomePesquisa, super.getUsuarioLogado(request));
				
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonNomePesquisa = objectMapper.writeValueAsString(lista);
				response.getWriter().write(jsonNomePesquisa);
			
			} else if (acao != null && !acao.isEmpty() && acao.equalsIgnoreCase("pesquisarParaEditar")) {						
				String id = request.getParameter("id");				
				ModelLogin modelLogin = daoUsuarioRepository.consultarUsuarioId(id, super.getUsuarioLogado(request));	
				
				List<ModelLogin> listaUsuarios = daoUsuarioRepository.listarUsuarios(super.getUsuarioLogado(request));
				request.setAttribute("listaUsuarios", listaUsuarios);
				
				request.setAttribute("msg", "Usu�rio em edi��o");
				request.setAttribute("modelLogin", modelLogin);
				request.getRequestDispatcher("principal/usuario.jsp").forward(request, response);				
				
			} else if (acao != null && !acao.isEmpty() && acao.equalsIgnoreCase("listarUsuarios")) {
				
				List<ModelLogin> listaUsuarios = daoUsuarioRepository.listarUsuarios(super.getUsuarioLogado(request));
				
				request.setAttribute("msg", "Lista de Usuarios");
				request.setAttribute("listaUsuarios", listaUsuarios);
				request.getRequestDispatcher("principal/usuario.jsp").forward(request, response);
				
			} else {
				List<ModelLogin> listaUsuarios = daoUsuarioRepository.listarUsuarios(super.getUsuarioLogado(request));
				request.setAttribute("listaUsuarios", listaUsuarios);				
				request.getRequestDispatcher("principal/usuario.jsp").forward(request, response);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			RequestDispatcher dispatcher = request.getRequestDispatcher("/erro.jsp");
			request.setAttribute("msg", e.getMessage());
			dispatcher.forward(request, response);
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			
			String msg = "Opera��o realizada com sucesso.";

			String id = request.getParameter("id");
			String nome = request.getParameter("nome");
			String email = request.getParameter("email");
			String login = request.getParameter("login");
			String senha = request.getParameter("senha");
			String perfil = request.getParameter("perfil");
			String sexo = request.getParameter("genero");

			ModelLogin modelLogin = new ModelLogin();
			
			modelLogin.setId(id != null && !id.isEmpty() ? Long.parseLong(id) : null);
			modelLogin.setNome(nome);
			modelLogin.setEmail(email);
			modelLogin.setLogin(login);
			modelLogin.setSenha(senha);
			modelLogin.setPerfil(perfil);
			modelLogin.setSexo(sexo);
			
			
					
			if(ServletFileUpload.isMultipartContent(request)) {
				
				
				Part part = request.getPart("arquivoFoto");	
		
				
				byte[] foto = new byte[(int) part.getSize()];
				
				/*try {
					
					part.getInputStream().read(foto);
					// String base64AsString = new String(Base64.encodeBase64String(foto));
					String base64AsString = "data:image/" + part.getContentType().split("\\/")[1] + ";base64," + new String(Base64.encodeBase64String(foto));
					
					modelLogin.setFotoUsuario(base64AsString);
					modelLogin.setExtensaoFotoUsuario(part.getContentType().split("\\/")[1]);
					
				} catch (Exception e) {
					// TODO: handle exception
				}*/
				
				part.getInputStream().read(foto);
				// String base64AsString = new String(Base64.encodeBase64String(foto));
				String base64AsString = "data:image/" + part.getContentType().split("\\/")[1] + ";base64," + new String(Base64.encodeBase64String(foto));
				
				modelLogin.setFotoUsuario(base64AsString);
				modelLogin.setExtensaoFotoUsuario(part.getContentType().split("\\/")[1]);
				
				
				
 			}
			
			if (daoUsuarioRepository.verificaLogin(modelLogin.getLogin()) && modelLogin.getId() == null) {
				msg = "Login j� existe";
			} else {

				if (modelLogin.isNovo()) {
					msg = "Novo usu�rio cadastrado.";
				} else {
					msg = "usu�rio alterado.";
				}

				modelLogin = daoUsuarioRepository.gravarUsuario(modelLogin, super.getUsuarioLogado(request));
			}
			
			List<ModelLogin> listaUsuarios = daoUsuarioRepository.listarUsuarios(super.getUsuarioLogado(request));
			request.setAttribute("listaUsuarios", listaUsuarios);

			request.setAttribute("msg", msg);
			request.setAttribute("modelLogin", modelLogin);
			request.getRequestDispatcher("principal/usuario.jsp").forward(request, response);

		} catch (Exception e) {
			e.printStackTrace();
			RequestDispatcher dispatcher = request.getRequestDispatcher("/erro.jsp");
			request.setAttribute("msg", e.getMessage());
			dispatcher.forward(request, response);
		}

	}

}
