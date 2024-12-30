package com.itextos.beacon.commonlib.encryption.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.itextos.beacon.commonlib.encryption.process.Processor;
import com.itextos.beacon.commonlib.pwdencryption.CryptoType;
import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;

@WebServlet("/encryption")
public class EncryptionServlet2 extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String cryptoType = request.getParameter("cryptotype");
        session.setAttribute("cryptotype", cryptoType);

        switch (cryptoType) {
            case "encode":
                handleEncode(request, session);
                break;
            case "decode":
                handleDecode(request, session);
                break;
            case "encrypt":
			try {
				handleEncrypt(request, session);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                break;
            case "decrypt":
			try {
				handleDecrypt(request, session);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                break;
        }

        response.sendRedirect("encryption.jsp");
    }

    private void handleEncode(HttpServletRequest request, HttpSession session) {
    	/*
        String encodeText = request.getParameter("encodetext");
        String encodedText = CommonUtility.base64Encode(encodeText);

        session.setAttribute("encodeText", encodeText);
        session.setAttribute("encodedText", encodedText);
        */
    }

    private void handleDecode(HttpServletRequest request, HttpSession session) {
    	/*
        String decodeText = request.getParameter("decodetext");
        String decodedText = Encryptor.base64Decode(decodeText);

        session.setAttribute("decodeText", decodeText);
        session.setAttribute("decodedText", decodedText);
    	*/
    }

    private void handleEncrypt(HttpServletRequest request, HttpSession session) throws Exception {
        String encryptText = request.getParameter("encrypttext");
        String encryptKey = request.getParameter("ekey");
        
        String encryptedText = Encryptor.encrypt(CryptoType.getCryptoType((String) session.getAttribute("cryptotype")),encryptText, encryptKey).toString();

        session.setAttribute("etext", encryptText);
        session.setAttribute("ekey", encryptKey);
        session.setAttribute("enryptedText", encryptedText);
    }

    private void handleDecrypt(HttpServletRequest request, HttpSession session) throws Exception {
        String decryptText = request.getParameter("decrypttext");
        String decryptKey = request.getParameter("dkey");
        String decryptedText = Encryptor.decrypt(CryptoType.getCryptoType((String) session.getAttribute("cryptotype")),decryptText, decryptKey).toString();

        session.setAttribute("dtext", decryptText);
        session.setAttribute("dkey", decryptKey);
        session.setAttribute("decryptedText", decryptedText);
    }
}
