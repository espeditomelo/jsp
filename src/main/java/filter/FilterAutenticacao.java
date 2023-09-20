package filter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import connection.SingleConnectionBD;

@WebFilter(urlPatterns = { "/principal/*" })
public class FilterAutenticacao extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

	private static Connection conn;

	public FilterAutenticacao() {
		super();
		// TODO Auto-generated constructor stub
	}

	// encerra processos quando o servidor for parado. exemplo: conexao
	public void destroy() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// intercepta as requisicoes e dar respostas
	// tudo deve passar por ele. exe.: validacao de autenticacao, commit, rollback
	// de bd
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {

			HttpServletRequest req = (HttpServletRequest) request;
			HttpSession session = req.getSession();

			String usuarioLogado = (String) session.getAttribute("usuario");

			// url que está sendo acessada
			String urlParaAutenticar = req.getServletPath();

			// varlidacao se está logado
			if (usuarioLogado == null && !urlParaAutenticar.equalsIgnoreCase("/principal/ServletLogin")) {

				RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp?url=" + urlParaAutenticar);
				request.setAttribute("msg", "Faça o login");
				dispatcher.forward(request, response);
				return; // para a execucao e redireciona para o login

			} else {
				// deixa processo do software continuar
				chain.doFilter(request, response);
			}

			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			RequestDispatcher dispatcher = request.getRequestDispatcher("erro.jsp");
			request.setAttribute("msg", e.getMessage());
			dispatcher.forward(request, response);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

	}

	// inicia quando os processos ou recursos quando o servidor executar
	public void init(FilterConfig fConfig) throws ServletException {
		conn = SingleConnectionBD.getConnection();
	}

}
