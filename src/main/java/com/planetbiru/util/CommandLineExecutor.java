package com.planetbiru.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.planetbiru.util.OSUtil.OS;

public class CommandLineExecutor {
	
	private CommandLineExecutor()
	{
		
	}

	public static CommandLineResult run(String command)
	{	
		CommandLineResult result = new CommandLineResult();
		String line;
        Process process;
        try 
        {
        	String commandLine = CommandLineExecutor.fixCommand(command);
            process = Runtime.getRuntime().exec(commandLine);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null)
            {
            	result.addLine(line);
            }
            process.waitFor();
            result.setExitValue(process.exitValue());
            process.destroy();
        } 
        catch (Exception e) {
        	result.setError(true);
        	result.setErrorMessage(e.getMessage());
        }
        return result;
	}
	
	public static CommandLineResult run(String[] command)
	{	
		CommandLineResult result = new CommandLineResult();
		String line;
        Process process;
        try 
        {
            process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null)
            {
            	result.addLine(line);
            }
            process.waitFor();
            result.setExitValue(process.exitValue());
            process.destroy();
        } 
        catch (Exception e) {
        	result.setError(true);
        	result.setErrorMessage(e.getMessage());
        }
        return result;
	}

	private static String fixCommand(String command) 
	{
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			return "cmd.exe /c "+command;
		}
		else
		{
			return command;
		}
	}
	public static String execSSH(String username, String password, String host, int port, String command, long sleep) throws JSchException 
	{
		Session session = null;
		ChannelExec channel = null;

		String responseString = "";
		try 
		{
			session = new JSch().getSession(username, host, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
			channel.setOutputStream(responseStream);
			channel.connect();

			while (channel.isConnected()) 
			{
				try 
				{
					Thread.sleep(sleep);
				} 
				catch (InterruptedException e) 
				{
					Thread.currentThread().interrupt();
				}
			}
			responseString = new String(responseStream.toByteArray());
		} 
		finally 
		{
			if (session != null) 
			{
				session.disconnect();
			}
			if (channel != null) 
			{
				channel.disconnect();
			}
		}
		return responseString;
	}
}
