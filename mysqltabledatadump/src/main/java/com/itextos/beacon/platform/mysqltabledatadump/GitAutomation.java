package com.itextos.beacon.platform.mysqltabledatadump;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.io.IOException;

public class GitAutomation {
	
    public static void pushMysql() {
        String repoPath = "/mysqldump/technowizardsmysqldump"; // Local repo path
        String remoteRepoUrl = "https://github.com/jaffersadhik/technowizardsmysqldump.git"; // GitHub repo URL
        String fileName="technowizardsmysqldump.zip";
        
        push(repoPath,remoteRepoUrl,fileName);
    }
    
    
    public static void pushRedis() {
    	
        String repoPath = "/redisdump/technowizardsredisdump"; // Local repo path
        String remoteRepoUrl = "https://github.com/jaffersadhik/technowizardsredisdump.git"; // GitHub repo URL
        String fileName="technowizardsredisdump.zip";
        
        push(repoPath,remoteRepoUrl,fileName);
    }
    
    public static void push(String repoPath,String remoteRepoUrl,String fileName) {
    	

        String commitMessage = "Added a new file";
        String branch = "master"; // Branch to push
        String username = "jaffer.sadhik@gmail.com";
        String personalAccessToken = System.getenv("token"); // Use a PAT

        try {
            Git git = Git.open(new File(repoPath));

            // Add file to git
            git.add().addFilepattern(fileName).call();

            // Commit changes
            git.commit().setMessage(commitMessage).call();

            // Push changes with authentication
            git.push()
                .setRemote(remoteRepoUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, personalAccessToken))
                .call();

            System.out.println("File added, committed, and pushed successfully!");
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}

