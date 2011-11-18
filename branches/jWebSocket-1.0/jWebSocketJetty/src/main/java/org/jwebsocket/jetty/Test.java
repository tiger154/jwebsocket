//	---------------------------------------------------------------------------
//	jWebSocket - Test Servlet for Jetty Demo App
//	Copyright (c) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.jetty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author aschulze
 */
public class Test extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        aResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter lOut = aResponse.getWriter();
        try {
            lOut.println("<html>");
            lOut.println("<head>");
            lOut.println("<title>Jetty Servlet Test</title>");
            lOut.println("</head>");
            lOut.println("<body>");
            lOut.println("<h1>Jetty Servlet Test at " + aRequest.getContextPath () + "</h1>");
            lOut.println("</body>");
            lOut.println("</html>");
        } finally { 
            lOut.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        processRequest(aRequest, aResponse);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        processRequest(aRequest, aResponse);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
