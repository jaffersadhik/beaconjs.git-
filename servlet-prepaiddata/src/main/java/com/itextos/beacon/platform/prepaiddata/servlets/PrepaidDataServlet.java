package com.itextos.beacon.platform.prepaiddata.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextos.beacon.platform.prepaiddata.PrepaidData;
import com.itextos.beacon.platform.prepaiddata.ReadRedisData;
import com.itextos.beacon.platform.prepaiddata.inmemory.CurrencyData;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class PrepaidDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        // Constants
        String CLI_ID_OR_USERNAME = "cliid_user";
        String RESULT = "result";
        String color_nouser_bgcolor = "#ff0000";
        String color_nouser = "#ffffff";
        String color_nonactive = "#3333ff";
        String color_lowBalance = "#ff0000";
        String color_others = "#339900";

        // Fetch data from session or Redis
        String cliidUser = CommonUtility.nullCheck(session.getAttribute(CLI_ID_OR_USERNAME), true);
        Map<String, Set<PrepaidData>> result = (Map<String, Set<PrepaidData>>) session.getAttribute(RESULT);

        // If no data in session, fetch from Redis
        if (result == null) {
       //     result = ReadRedisData.fetchPrepaidData(cliidUser);
            session.setAttribute(RESULT, result);
        }

        // Generate HTML
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='ISO-8859-1'>");
        out.println("<title>Prepaid Data Sheet</title>");
        out.println("<style>");
        out.println("table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; }");
        out.println("th { border: 1px solid #808080; background-color: #dddddd; padding: 8px; }");
        out.println("td { border: 1px solid #808080; padding: 8px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body class='bodycolor'>");

        // Header
        out.println("<table align='center' width='100%'>");
        out.println("<tr><td align='center'><font face='arial, sans-serif'><h1>Beacon Prepaid Details</h1></font></td></tr>");
        out.println("<tr><font face='arial, sans-serif'><td align='right'>Last refreshed at</font>");
        out.println("<font face='arial, sans-serif' color='blue'>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</font></td></tr>");
        out.println("</table>");
        out.println("<br><br>");

        // Form
        out.println("<form name='interfaceform' method='post' action='prepaiddatafetchservlet'>");
        out.println("<table align='center' width='100%'>");
        out.println("<tr><td align='right'><font face='arial, sans-serif'>Enter Client Id / Username :</font></td>");
        out.println("<td><font face='arial, sans-serif'><input type='text' value='" + cliidUser + "' name='" + CLI_ID_OR_USERNAME + "' id='" + CLI_ID_OR_USERNAME + "'></font>");
        out.println("<button type='reset' name='reset'>Reset</button>");
        out.println("<button type='submit' name='prepaiddatafetchservlet'>Submit</button></td></tr>");
        out.println("</table>");
        out.println("<br><br>");

        // Table
        out.println("<table align='center' width='100%' border='1'>");
        out.println("<tr><th>Client Id</th><th>User</th><th>Currency</th><th>Balance</th><th>Status</th></tr>");

        if (result != null) {
            DecimalFormat df = new DecimalFormat("###,###,##0.000000");

            // No User Data
            printPrepaidData(out, result.get(ReadRedisData.NO_USER), color_nouser_bgcolor, color_nouser, df);

            // Non-Active User Data
            printPrepaidData(out, result.get(ReadRedisData.NON_ACTIVE), null, color_nonactive, df);

            // Low Balance User Data
            printPrepaidData(out, result.get(ReadRedisData.LOW_BALANCE), null, color_lowBalance, df);

            // Other Balance User Data
            printPrepaidData(out, result.get(ReadRedisData.OTHER_BALANCE), null, color_others, df);
        } else {
            out.println("<tr><td colspan='5' align='center'>No data found</td></tr>");
        }

        out.println("</table>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }

    private void printPrepaidData(PrintWriter out, Set<PrepaidData> prepaidDataSet, String bgcolor, String fontColor, DecimalFormat df) {
        if (prepaidDataSet != null && !prepaidDataSet.isEmpty()) {
            for (PrepaidData pd : prepaidDataSet) {
                CurrencyData cd = pd.getCurrencyInfo();
                out.println("<tr>");
                out.println("<td" + (bgcolor != null ? " bgcolor='" + bgcolor + "'" : "") + "><font color='" + fontColor + "'>" + pd.getCliId() + "</font></td>");
                out.println("<td" + (bgcolor != null ? " bgcolor='" + bgcolor + "'" : "") + "><font color='" + fontColor + "'>" + CommonUtility.nullCheck(pd.getUserName(), true) + "</font></td>");
                out.println("<td" + (bgcolor != null ? " bgcolor='" + bgcolor + "'" : "") + "><font color='" + fontColor + "'>" + (cd == null ? "N/A" : cd.getCode() + "-" + cd.getDesc()) + "</font></td>");
                out.println("<td" + (bgcolor != null ? " bgcolor='" + bgcolor + "'" : "") + " align='right'><font color='" + fontColor + "'>" + df.format(pd.getPrepaidBalance()) + "</font></td>");
                out.println("<td" + (bgcolor != null ? " bgcolor='" + bgcolor + "'" : "") + " align='center'><font color='" + fontColor + "'>" + pd.getAccountStatus() + "</font></td>");
                out.println("</tr>");
            }
        }
    }
}
