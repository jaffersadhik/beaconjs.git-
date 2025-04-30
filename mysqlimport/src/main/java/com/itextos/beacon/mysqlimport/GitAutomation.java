package com.itextos.beacon.mysqlimport;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.io.IOException;
import java.util.Date;
public class GitAutomation {
	
	public static void pullMysql() {
        String repoPath = "/mysqldump/technowizardsmysqldump"; // Local repo path
        String remoteRepoUrl = "https://github.com/jaffersadhik/technowizardsmysqldump.git"; // GitHub repo URL
        String fileName="technowizardsmysqldump.zip";
        
        pull(repoPath,remoteRepoUrl,fileName);
    }
	
    public static void pushMysql() {
        String repoPath = "/mysqldump/technowizardsmysqldump"; // Local repo path
        String remoteRepoUrl = "https://github.com/jaffersadhik/technowizardsmysqldump.git"; // GitHub repo URL
        String fileName="technowizardsmysqldump.zip";
        
        push(repoPath,remoteRepoUrl,fileName);
    }
    
    
    public static void pushRedis() {
    	
        String repoPath = "/redisdump/technowizardsredisdump"; // Local repo path
        String remoteRepoUrl = "https://github.com/jaffersadhik/technowizardsredisdump.git"; // GitHub repo URL
        String fileName1="technowizardswalletredisdump.zip";
        String fileName2="technowizardsgeneralredisdump.zip";

        push(repoPath,remoteRepoUrl,fileName1,fileName2);
    }
    
    public static void push(String repoPath,String remoteRepoUrl,String fileName) {
    	

        String commitMessage = new Date()+"";
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
    
public static void push(String repoPath,String remoteRepoUrl,String fileName1,String fileName2) {
    	

        String commitMessage = new Date()+"";
        String branch = "master"; // Branch to push
        String username = "jaffer.sadhik@gmail.com";
        String personalAccessToken = System.getenv("token"); // Use a PAT

        try {
            Git git = Git.open(new File(repoPath));

            // Add file to git
            git.add().addFilepattern(fileName1).call();
            
            git.add().addFilepattern(fileName2).call();


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




public static void pull(String repoPath,String remoteRepoUrl,String fileName) {
	

    String commitMessage = new Date()+"";
    String branch = "master"; // Branch to push
    String username = "jaffer.sadhik@gmail.com";
    String personalAccessToken = System.getenv("token"); // Use a PAT

    try {
        Git git = Git.open(new File(repoPath));

     // Perform the pull operation
        PullResult result = git.pull().call();

        if (result.isSuccessful()) {
            System.out.println("Git pull successful.");
        } else {
            System.out.println("Git pull failed.");
        }
        git.close();


        System.out.println("git pull successfully!");
    } catch (IOException | GitAPIException e) {
        e.printStackTrace();
    }
}
}

