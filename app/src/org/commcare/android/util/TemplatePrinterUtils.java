package org.commcare.android.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.commcare.android.crypt.CryptUtil;
import org.commcare.dalvik.R;
import org.commcare.dalvik.dialogs.AlertDialogFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Various utilities used by TemplatePrinterTask and TemplatePrinterActivity
 *
 * @author Richard Lu
 * @author amstone
 */
public abstract class TemplatePrinterUtils {

    private static final String FORMAT_REGEX_WITH_DELIMITER = "((?<=%2$s)|(?=%1$s))";
    private static final SecretKey KEY = CryptUtil.generateSemiRandomKey();

    /**
     * Concatenate all Strings in a String array to one String.
     *
     * @param strings String array to join
     * @return Joined String
     */
    public static String join(String[] strings) {
        return TextUtils.join("", strings);
    }

    /**
     * Remove all occurrences of the specified String segment
     * from the input String.
     *
     * @param input    String input to remove from
     * @param toRemove String segment to remove
     * @return input with all occurrences of toRemove removed
     */
    public static String remove(String input, String toRemove) {
        return TextUtils.join("", input.split(toRemove));
    }

    /**
     * Split a String while keeping the specified start and end delimiters.
     * <p/>
     * Sources:
     * http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
     *
     * @param input          String to split
     * @param delimiterStart Start delimiter; will split immediately before this delimiter
     * @param delimiterEnd   End delimiter; will split immediately after this delimiter
     * @return Split string array
     */
    public static String[] splitKeepDelimiter(
            String input,
            String delimiterStart,
            String delimiterEnd) {

        String delimiter = String.format(FORMAT_REGEX_WITH_DELIMITER, delimiterStart, delimiterEnd);

        return input.split(delimiter);
    }

    /**
     * @param file the input file
     * @return A string representation of the entire contents of the file
     * @throws IOException
     */
    public static String docToString(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        while ((str = in.readLine()) != null) {
            builder.append(str);
        }
        in.close();
        return builder.toString();
    }

    /**
     * Writes the given string, encrypted, to the file location specified
     */
    public static void writeStringToFile(String fileText, String outputPath) throws IOException {
        try {
            Cipher encrypter = Cipher.getInstance("AES");
            encrypter.init(Cipher.ENCRYPT_MODE, KEY);
            FileOutputStream fos = new FileOutputStream(new File(outputPath));
            CipherOutputStream cos = new CipherOutputStream(fos, encrypter);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cos));
            out.write(fileText);
            out.close();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads back from the encrypted file generated by the above and returns a string
     * representation of the file's contents
     */
    public static String readStringFromFile(String readFromPath) throws IOException {
        try {
            Cipher decrypter = Cipher.getInstance("AES");
            decrypter.init(Cipher.DECRYPT_MODE, KEY);
            FileInputStream fis = new FileInputStream(new File(readFromPath));
            CipherInputStream cis = new CipherInputStream(fis, decrypter);
            BufferedReader reader = new BufferedReader(new InputStreamReader(cis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Shows a pop-up dialog from the given activity that can optionally finish the activity when
     * it is dismissed by the user
     */
    public static void showAlertDialog(final Activity activity, String title, String msg,
                                       final boolean finishActivity) {
        AlertDialogFactory.getBasicAlertFactory(activity, title, msg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (finishActivity) {
                    activity.finish();
                }
            }
        }).showDialog();
    }

    public static void showPrintStatusDialog(final Activity activity, String title, String msg,
                                             final boolean reportSuccess) {
        AlertDialogFactory.getBasicAlertFactory(activity, title, msg,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                Bundle responses = new Bundle();
                responses.putString("print_executed", "" + reportSuccess);
                intent.putExtra("odk_intent_bundle", responses);
                activity.setResult(activity.RESULT_OK, intent);
                activity.finish();
            }
        }).showDialog();

    }

}