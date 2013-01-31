package com.mobiarch.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("*.html")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass().getName());
   
	private String supportedLanguages[];
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String servletPath = request.getServletPath();
		
		logger.fine("Servlet path: " + servletPath);
		
		String lang = null;
		
		//See if user wants to switch language
		lang = request.getParameter("language");
		if (lang == null || lang.length() == 0) {
			lang = null;
		} else {
			logger.fine("User is attempting to switch language to: " + lang);
		}
		
		//Try to get JSTL's language
		if (lang == null) {
			logger.fine("Trying to get JSTL's locale");
			Locale l = getJSTLLocale(request);
			
			if (l != null) {
				lang = l.getLanguage();
			}
		}

		//Try to get language from browser
		if (lang == null) {
			Locale l = request.getLocale();
			logger.fine("Browser's locale: " + l.toString());
			
			lang = l.getLanguage();
		}
		//See if the language is supported
		if (lang != null) {
			boolean supported = false;
			for (String supLang : supportedLanguages) {
				if (supLang.equals(lang)) {
					supported = true;
					break;
				}
			}
			if (supported == false) {
				logger.fine("Unsupported language: " + lang);
				lang = null;
			}
		}

		//If language is still not found, use the
		//first from the list of languages
		if (lang == null) {
			lang = supportedLanguages[0];
			logger.fine("Defaulting language to: " + lang);
		}
		
		//Update JSTL's locale to the computed one.
		logger.fine("Final computed language: " + lang);
		Config.set(request.getSession(), Config.FMT_LOCALE, new java.util.Locale(lang));
		
		String jsp = servletPath.replaceFirst("\\.html", "_" + lang + ".jsp");
		logger.fine("Loading JSP: " + jsp);
		
		request.getRequestDispatcher(jsp).forward(request, response);
	}
	
	public Locale getJSTLLocale(HttpServletRequest request) {
		//Try to get the JSTL locale
		HttpSession session = request.getSession(false);
		if (session != null) {
			Locale l = (Locale) Config.get(session, Config.FMT_LOCALE);
			if (l != null) {
				logger.fine("JSTL locale: " + l.toString());
				
				return l;
			}
		}
		
		logger.fine("JSTL locale is not set.");
		
		return null;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String str = 
				config.getServletContext().getInitParameter("supported-languages");
		if (str == null) {
			throw new ServletException("Context parameter supported-languages is not set.");
		}
		
		supportedLanguages = str.split(",");
		//Trim the strings
		for (int i = 0; i < supportedLanguages.length; ++i) {
			supportedLanguages[i] = supportedLanguages[i].trim();
		}
	}
}
