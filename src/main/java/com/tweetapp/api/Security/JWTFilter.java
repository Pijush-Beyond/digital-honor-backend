package com.tweetapp.api.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.GenericFilterBean;

import com.tweetapp.api.service.TokenService;

@Configuration
public class JWTFilter extends GenericFilterBean {

	private TokenService tokenService;
//	private static final String[] AUTH_WHITELIST = {
//	        "/authenticate",
//	        "/swagger-resources/**",
//	        "/swagger-ui/**",
//	        "/v3/api-docs",
//	        "/webjars/**"
//	};

	JWTFilter() {
		this.tokenService = new TokenService();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); //before value "*"
		response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
		response.setHeader("Access-Control-Allow-Methods", "GET,POST,PATCH,DELETE,PUT,OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
		response.setHeader("Access-Control-Max-Age", "86400");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		// List<String> token = new ArrayList<String>();
		// if(request.getCookies()!=null)
		// 	token = Arrays.asList(request.getCookies()).stream().filter(cookie-> cookie.getName().equals("authCookie")).map(cookie-> cookie.getValue()).toList();

		if (!StringUtils.isEmpty(token)) {
			String[] tokens = token.split(" ");
			token = tokens[1];
		}

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.sendError(HttpServletResponse.SC_OK, "success");
			return;
		}

		if (allowRequestWithoutToken(request)) {
			response.setStatus(HttpServletResponse.SC_OK);
			filterChain.doFilter(req, res);
		} else {
			if (token == null || !tokenService.isTokenValid(token)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				ObjectId userId = new ObjectId(tokenService.getUserIdFromToken(token));
				request.setAttribute("userId", userId);
				filterChain.doFilter(req, res);

			}

			// if (token.size()==0 || !tokenService.isTokenValid(token.get(0))) {
			// 	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			// } else {
			// 	ObjectId userId = new ObjectId(tokenService.getUserIdFromToken(token.get(0)));
			// 	request.setAttribute("userId", userId);
			// 	filterChain.doFilter(req, res);

			// }
		}

	}

	public boolean allowRequestWithoutToken(HttpServletRequest request) {
		// System.out.println("\n\n\n"+request.getRequestURI()+"\n\n\n");
		if (request.getRequestURI().contains("/login") || request.getRequestURI().contains("/register")
				|| request.getRequestURI().contains("/swagger-resources") ||request.getRequestURI().contains("/swagger-ui")
				|| request.getRequestURI().contains("/v2/api-docs")  ) {
			return true;
		}
		return false;
	}
}