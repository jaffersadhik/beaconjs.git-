package com.itextos.beacon.platform.prepaiddata.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfo;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.KannelInfoLoader;
import com.itextos.beacon.platform.prepaiddata.kannelstatus.ReadKannelInfo;

@WebServlet("/kannelinfo")
public class KannelInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        // Fetch data from session or loader
        Map<String, List<KannelInfo>> kannelInfoMap = (Map<String, List<KannelInfo>>) session.getAttribute("kannelinfo");
        List<String> operators = (List<String>) session.getAttribute("operators");
        List<String> routeList = (List<String>) session.getAttribute("routes");

        if (operators == null) {
            operators = new ArrayList<>();
        }
        if (routeList == null) {
            routeList = new ArrayList<>();
        }

        // Fetch all operators
        Set<String> allOperators = KannelInfoLoader.getInstance().getAllOperators();

        // Fetch refresh interval
        String refreshIntervalObj = request.getParameter("refresh");
        refreshIntervalObj = refreshIntervalObj != null ? refreshIntervalObj : "30";

        if (!"-1".equals(refreshIntervalObj)) {
            response.setIntHeader("Refresh", CommonUtility.getInteger(refreshIntervalObj, 30));
        }

        // Generate HTML
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='ISO-8859-1'>");
        out.println("<title>Kannel Info</title>");
        out.println("<style>");
        out.println("table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; }");
        out.println("th { border: 1px solid #808080; background-color: #dddddd; padding: 8px; }");
        out.println("td { border: 1px solid #808080; padding: 8px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body class='bodycolor'>");

        // Header
        out.println("<table align='center' width='100%'>");
        out.println("<tr><td align='center'><font face='arial, sans-serif'><h1>Beacon Kannel Details</h1></font></td></tr>");
        out.println("<tr><td align='right'><font face='arial, sans-serif'>Last refreshed at : </font>");
        out.println("<font face='arial, sans-serif' color='blue'>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</font></td></tr>");
        out.println("</table>");
        out.println("<br>");

        // Form
        out.println("<form name='kannelinfoform' id='kannelinfoform' method='post' action='kannelinfofetchservlet'>");
        out.println("<table align='center' width='100%'>");
        out.println("<tr><td align='right' colspan='4'><font face='arial, sans-serif'>Refresh Interval &nbsp;");
        out.println("<select name='refresh' id='refresh'>");
        out.println("<option value='5'" + ("5".equals(refreshIntervalObj) ? " selected" : "") + ">Every 5 seconds</option>");
        out.println("<option value='10'" + ("10".equals(refreshIntervalObj) ? " selected" : "") + ">Every 10 seconds</option>");
        out.println("<option value='30'" + ("30".equals(refreshIntervalObj) ? " selected" : "") + ">Every 30 seconds</option>");
        out.println("<option value='-1'" + ("-1".equals(refreshIntervalObj) ? " selected" : "") + ">Don't refresh</option>");
        out.println("</select></font></td></tr>");

        // Operators and Routes
        out.println("<tr><td align='center'><font face='arial, sans-serif'>Operator</font></td>");
        out.println("<td><font face='arial, sans-serif'><select name='operator' id='operator' multiple onchange='getSelectedOperator()'>");
        for (String operator : allOperators) {
            out.println("<option value='" + operator + "'" + (operators.contains(operator) ? " selected" : "") + ">" + operator + "</option>");
        }
        out.println("</select></font></td>");

        out.println("<td align='center'><font face='arial, sans-serif'>Route</font></td>");
        out.println("<td><font face='arial, sans-serif'><select name='route' id='route' multiple onchange='getSelectedRoute()'>");
        if (operators.isEmpty()) {
            for (String operator : allOperators) {
                List<String> routeIds = KannelInfoLoader.getInstance().getRoutesForOperator(operator);
                for (String routeId : routeIds) {
                    out.println("<option value='" + routeId + "'>" + routeId + "</option>");
                }
            }
        } else {
            for (String operator : operators) {
                List<String> routeIds = KannelInfoLoader.getInstance().getRoutesForOperator(operator);
                for (String routeId : routeIds) {
                    out.println("<option value='" + routeId + "'" + (routeList.contains(routeId) ? " selected" : "") + ">" + routeId + "</option>");
                }
            }
        }
        out.println("</select></font></td></tr>");

        // Submit and Reset Buttons
        out.println("<tr><td align='center' colspan='2'><font face='arial, sans-serif'><input type='reset' id='reset' value='Reset' onClick='reset()'></font></td>");
        out.println("<td align='center' colspan='2'><font face='arial, sans-serif'><input type='submit' id='submit' value='Submit' onClick='submitForm()'></font></td></tr>");
        out.println("</table>");
        out.println("</form>");
        out.println("<br><br>");

        // Table for Kannel Info
        out.println("<table align='center' width='100%'>");
        out.println("<tr><th>Operator</th><th>Route</th><th>Available Status</th><th align='right'>Store Size</th><th align='center'>Kannel IP</th><th align='right'>Kannel Port</th><th align='right'>Kannel Status Port</th><th align='center'>Last Updated</th></tr>");

        if (kannelInfoMap != null) {
            List<KannelInfo> unavailableKannels = kannelInfoMap.get(ReadKannelInfo.UNAVAILABLE_KANNELS);
            List<KannelInfo> availableKannels = kannelInfoMap.get(ReadKannelInfo.AVAILABLE_KANNELS);

            // Unavailable Kannels
            out.println("<tr><td colspan='4' align='center' bgcolor='#ccffcc'><font face='arial, sans-serif' color='red'><b>Unavailable : " + (unavailableKannels == null ? "0" : unavailableKannels.size()) + "</b></font></td>");
            out.println("<td colspan='4' align='center' bgcolor='#ccffcc'><font face='arial, sans-serif' color='green'><b>Available : " + (availableKannels == null ? "0" : availableKannels.size()) + "</b></font></td></tr>");

            if (unavailableKannels != null && !unavailableKannels.isEmpty()) {
                for (KannelInfo kannelInfo : unavailableKannels) {
                    out.println("<tr>");
                    out.println("<td><font face='arial, sans-serif' color='red'>" + kannelInfo.getOperator() + "</font></td>");
                    out.println("<td><font face='Courier New, Courier, Consolas, Verdana' color='red'>" + kannelInfo.getRoute() + "</font></td>");
                    out.println("<td><font face='arial, sans-serif' color='red'>Unavailable</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='red'>" + kannelInfo.getStoreSize() + "</font></td>");
                    out.println("<td align='center'><font face='arial, sans-serif' color='red'>" + kannelInfo.getKannalIp() + "</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='red'>" + kannelInfo.getKannelPort() + "</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='red'>" + kannelInfo.getKannelStatusPort() + "</font></td>");
                    out.println("<td align='center'><font face='arial, sans-serif' color='red'>" + kannelInfo.getLastUpdated() + "</font></td>");
                    out.println("</tr>");
                }
            }

            // Available Kannels
            if (availableKannels != null && !availableKannels.isEmpty()) {
                for (KannelInfo kannelInfo : availableKannels) {
                    String color = kannelInfo.getStoreSize() < 0 ? "blue" : "green";
                    out.println("<tr>");
                    out.println("<td><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getOperator() + "</font></td>");
                    out.println("<td><font face='Courier New, Courier, Consolas, Verdana' color='" + color + "'>" + kannelInfo.getRoute() + "</font></td>");
                    out.println("<td><font face='arial, sans-serif' color='" + color + "'>Available</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getStoreSize() + "</font></td>");
                    out.println("<td align='center'><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getKannalIp() + "</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getKannelPort() + "</font></td>");
                    out.println("<td align='right'><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getKannelStatusPort() + "</font></td>");
                    out.println("<td align='center'><font face='arial, sans-serif' color='" + color + "'>" + kannelInfo.getLastUpdated() + "</font></td>");
                    out.println("</tr>");
                }
            }
        } else {
            out.println("<tr><td colspan='8' align='center'>No data found</td></tr>");
        }

        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }
}
