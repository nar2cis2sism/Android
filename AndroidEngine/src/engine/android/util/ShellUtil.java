package engine.android.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public final class ShellUtil {

    /**
     * 执行Root命令
     */
    public static boolean exeRootCommand(String command) {
        return exeCommand(new String[] { command }, true, null);
    }

    /**
     * execute shell commands(Will block)
     * 
     * @param commands Command array
     * @param isRoot Whether need to run with root
     * @param result If set, will store the result info
     * @return Whether execute successfully
     */
    public static boolean exeCommand(String[] commands, boolean isRoot,
            CommandResult result) {
        boolean success = false;
        int res = -1;

        if (commands == null || commands.length == 0)
        {
            if (result != null)
            {
                result.setResult(res, null, "空命令");
            }

            return success;
        }

        java.lang.Process process = null;
        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            dos = new DataOutputStream(process.getOutputStream());
            for (String command : commands)
            {
                if (TextUtils.isEmpty(command))
                {
                    continue;
                }

                // Do not use dos.writeBytes(commmand), avoid chinese charset error
                dos.write(command.getBytes());
                dos.writeBytes("\n");
                dos.flush();
            }

            dos.writeBytes("exit\n");
            dos.flush();
            success = (res = process.waitFor()) == 0;
            if (result != null)
            {
                StringBuilder responseMsg = new StringBuilder();
                StringBuilder errorMsg = new StringBuilder();
                try {
                    BufferedReader responseReader = null;
                    BufferedReader errorReader = null;
                    try {
                        responseReader = new BufferedReader(new InputStreamReader(
                                process.getInputStream()));
                        errorReader = new BufferedReader(new InputStreamReader(
                                process.getErrorStream()));
                        String s;
                        while ((s = responseReader.readLine()) != null)
                        {
                            responseMsg.append(s);
                        }

                        while ((s = errorReader.readLine()) != null)
                        {
                            errorMsg.append(s);
                        }
                    } finally {
                        if (responseReader != null)
                        {
                            responseReader.close();
                        }

                        if (errorReader != null)
                        {
                            errorReader.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result.setResult(res, responseMsg.toString(), errorMsg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null)
            {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (process != null)
            {
                process.destroy();
            }
        }

        return success;
    }

    public static final class CommandResult {

        private int result;

        private String responseMsg;

        private String errorMsg;

        void setResult(int result, String responseMsg, String errorMsg) {
            this.result = result;
            this.responseMsg = responseMsg;
            this.errorMsg = errorMsg;
        }

        public int getResult() {
            return result;
        }

        public String getResponseMsg() {
            return responseMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }
}