package com.itextos.beacon.commonlib.pwdencryption;

import java.util.Base64;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class Encryptor
{

    // private static final short MAX_ITERATIONN = 10;

    private Encryptor()
    {}

    public static EncryptedObject encrypt(
            CryptoType aCryptoType,
            String aToEncrypt,
            String aKey)
            throws Exception
    {
        EncryptedObject returnValue = null;

        switch (aCryptoType)
        {
            case ENCRYPTION_AES_256:
                returnValue = Aes256Encrypt.encrypt(aToEncrypt, aKey);
                break;

            case HASHING_BCRYPT:
                returnValue = BcryptHashing.hash(aToEncrypt);
                break;

            case EMPTY:
                returnValue = new EncryptedObject(aToEncrypt, "");
                break;

            case ENCODE:
                returnValue = new EncryptedObject(aToEncrypt, Base64.getEncoder().encodeToString(aToEncrypt.getBytes()));
                break;

            default:
                throw new ItextosRuntimeException("Invalid crypto type specified. CryptoType : '" + aCryptoType + "'");
        }
        return returnValue;
    }

    public static String decrypt(
            CryptoType aCryptoType,
            String aToDecrypt,
            String aKey)
            throws Exception
    {
        String returnValue = null;

        switch (aCryptoType)
        {
            case ENCRYPTION_AES_256:
                returnValue = Aes256Encrypt.decrypt(aToDecrypt, aKey);
                break;

            case ENCODE:
                returnValue = new String(Base64.getDecoder().decode(aToDecrypt));
                break;

            case HASHING_BCRYPT:
            case EMPTY:
                throw new ItextosRuntimeException("Not applicable for decrypt. CryptoType : '" + aCryptoType + "'");

            default:
                throw new ItextosRuntimeException("Invalid crypto type specified. CryptoType : '" + aCryptoType + "'");
        }
        return returnValue;
    }

    public static EncryptedObject getEncryptedDbPassword(
            String aDbPassword)
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.DB_PASSWORD_KEY);
    }

    public static String getDecryptedDbPassword(
            String aEncryptedDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aEncryptedDbPassword, PasswordConstants.DB_PASSWORD_KEY);
    }

    public static EncryptedObject getApiPassword()
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, RandomString.getApiPassword(), PasswordConstants.API_PASSWORD_KEY);
    }

    public static String getApiDecryptedPassword(
            String aDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.API_PASSWORD_KEY);
    }

    public static EncryptedObject getSmppPassword()
            throws Exception
    {
        return encrypt(CryptoType.ENCRYPTION_AES_256, RandomString.getSmppPassword(), PasswordConstants.SMPP_PASSWORD_KEY);
    }

    public static String getSmppDecryptedPassword(
            String aDbPassword)
            throws Exception
    {
        return decrypt(CryptoType.ENCRYPTION_AES_256, aDbPassword, PasswordConstants.SMPP_PASSWORD_KEY);
    }

    public static EncryptedObject getGuiPassword()
            throws Exception
    {
        return encrypt(CryptoType.HASHING_BCRYPT, RandomString.getGuiPassword(), null);
    }
    
    public static String getGuiDecryptedPassword(String pass)
            throws Exception
    {
        return decrypt(CryptoType.ENCODE, pass, null);
    }

    public static void main(
            String[] args)
    {

        try
        
        
        

        {
        /*	String type=args[0];
        	String encode=args[1];
        	
        	if(type.equals("db")) {
        		
        		  final String lDecryptedDbPassword = getDecryptedDbPassword(encode);
                  System.out.println(lDecryptedDbPassword);
                  
        	}else if(type.equals("smpp")) {
        	
      		  final String lDecryptedDbPassword = getSmppDecryptedPassword(encode);
              System.out.println(lDecryptedDbPassword);
    
        	}else if(type.equals("api")) {
        	
      		  final String lDecryptedDbPassword = getApiDecryptedPassword(encode);
              System.out.println(lDecryptedDbPassword);
    
        	}*/

        	
        	final String lDecryptedDbPassword = getDecryptedDbPassword("YSrU1+RIM5hwN+ycQQdy4XhlOGhoU3RrYmtELgjZwSBQBXSHJ8AUHk/I");
            System.out.println(lDecryptedDbPassword);
            final String s = getSmppDecryptedPassword("O6sDdq6P039GHmA5nu4iT3pMcjl5aEFiRFq9bNtoyT0vISytTdngS5LB");
            System.out.println("smpp : '" + s + "'\t");
            
            
           String s2= getGuiDecryptedPassword("$2a$10$nlCb67l1JggY/QzqdZq8quVfXatLjndhES/qvXY9IfsTzPXXkrRpi");

                   System.out.println("'" + s2 + "'\t");

            final String d = getApiDecryptedPassword("rUyujabtn7LkbCSYuQDhVnpTbTJHVm5fcF/P4IbhPzZ87zo4HeI2vurr");
            System.out.println("'" + d + "'\t");
            
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main1(
            String[] args)
    {

        try
        {
            final EncryptedObject lEncryptedObject        = getSmppPassword();
            final String          lActualString           = lEncryptedObject.getActualString();
            final String          lEncryptedWithIvAndSalt = lEncryptedObject.getEncryptedWithIvAndSalt();
            final String          s                       = getSmppDecryptedPassword(lEncryptedWithIvAndSalt);
       //     System.out.println("'" + lActualString + "', '" + lEncryptedWithIvAndSalt + "', '" + s + "'\t" + lActualString.equals(s));

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        
    }

}