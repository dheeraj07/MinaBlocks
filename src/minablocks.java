import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

import static com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER;
import static java.lang.Math.max;


public class minablocks {

    private static Map<String, String> addressRanges;
    public static String rootPath = "/root";
    public static String fetchAndSavePath = "/Users/dheeraj-9768/Documents/Mina-Logs/";


    public static void loadDataFromFiles() throws Exception
    {
        InputStream inputStream = new FileInputStream(new File("serverAddresses.yml"));//No I18N
        Yaml yaml = new Yaml();
        addressRanges = (Map<String, String>) yaml.load(inputStream);
        LOGGER.log(Level.INFO, "FILE: orgToFileMapping: "+addressRanges);
    }


    public static void saveFile(String fileName, int noOfMissingBlocks, String fetchAndSavePath, String timeStamp) throws Exception
    {
        String missedBlocksInThisRun = "Total number of missing blocks in "+fileName + " are :  " + noOfMissingBlocks;
        fileName = fileName.substring(0, fileName.length() - 4) + "_" + timeStamp;
        BufferedWriter writer = new BufferedWriter(new FileWriter(fetchAndSavePath + fileName + "_REPORT.txt", true));
        writer.append("\n");
        writer.append(missedBlocksInThisRun);
        writer.close();
    }

   public static void getFilesFromServer() throws Exception
   {
       for (Map.Entry<String, String> entry : addressRanges.entrySet())
       {
           JSch jsch = new JSch();
           Session session = null;
           session = jsch.getSession("root", entry.getKey(), 22);
           session.setTimeout(30 * 1000);
           session.setConfig("StrictHostKeyChecking", "no");
           session.setPassword(entry.getValue());
           session.connect();
           Channel channel = session.openChannel("sftp");
           channel.connect();
           ChannelSftp sftpChannel = (ChannelSftp) channel;

           Vector fileList = sftpChannel.ls(rootPath);
           for (int i = 0; i < fileList.size(); i++)
           {
               String fileName = fileList.get(i).toString();
               if (fileName.endsWith(".log"))
               {
                   String newFileName = "";
                   for (int j = fileName.length() - 1; j > 0; j--)
                   {
                       Character ch = fileName.charAt(j);
                       if (ch.equals(' '))
                       {
                           break;
                       }
                       newFileName += fileName.charAt(j);
                   }
                   System.out.println("Copying...   " + rootPath + "/" + new StringBuilder(newFileName).reverse().toString());
                   sftpChannel.get(rootPath + "/" + new StringBuilder(newFileName).reverse().toString(), fetchAndSavePath + new StringBuilder(newFileName).reverse().toString());
               }
           }
           sftpChannel.exit();
           session.disconnect();
       }
   }


   public static Integer supportFunctionForProcessingBlockNumber(Scanner sc)
   {
       Integer refBlock = 0;
       while (sc.hasNext())
       {
           String blockMessage = sc.nextLine();
           if (blockMessage.contains("Finished!"))
           {
               int index = blockMessage.indexOf('p');
               index += 2;
               String blockNumber = "";
               while (blockMessage.charAt(index) != '.')
               {
                   blockNumber += blockMessage.charAt(index);
                   index++;
               }
               Integer currBlock = Integer.parseInt(blockNumber);
               if(!refBlock.equals(0))
               {
                   if(currBlock>=refBlock)
                   {
                       return refBlock;
                   }
               }
               refBlock = currBlock;
           }
       }
       return null;
   }

   public static Integer getInitialBlockNumber(List<String> fileNames) throws Exception
   {
       Integer minBlockValueToConsider = Integer.MIN_VALUE;
       for (String fileName : fileNames)
       {
           File blocks = new File(fetchAndSavePath + fileName);
           Scanner sc = new Scanner(blocks);
           minBlockValueToConsider = max(minBlockValueToConsider, supportFunctionForProcessingBlockNumber(sc));
       }
       return minBlockValueToConsider;
   }


    public static void analyseBlocksData(List<String> fileNames) throws Exception
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd - HH.mm.ss").format(new Date());
        Set<Integer> missedBlocks = new HashSet<>();
        Set<Integer> presentBlocks = new HashSet<>();
        Integer initialBlock = getInitialBlockNumber(fileNames);
        System.out.println("Starting analysing from Block number:    "+initialBlock);

        for (String fileName : fileNames)
        {
            Integer block = initialBlock;
            File blocks = new File(fetchAndSavePath + fileName);
            Scanner sc = new Scanner(blocks);
            int noOfMissingBlocks = 0;
            System.out.println("======================================================="+fileName+"=======================================================\n\n");
            while (sc.hasNext())
            {
                String blockMessage = sc.nextLine();
                if (blockMessage.contains("Finished!"))
                {
                    int index = blockMessage.indexOf('p');
                    index += 2;
                    String blockNumber = "";
                    while (blockMessage.charAt(index) != '.')
                    {
                        blockNumber += blockMessage.charAt(index);
                        index++;
                    }
                    Integer currBlock = Integer.parseInt(blockNumber);
                    if (currBlock - block > 1)
                    {
                        System.out.print("Prev Block: "+block);
                        System.out.print(",     Curr Block: "+currBlock);
                        noOfMissingBlocks += currBlock - block-1;
                        System.out.println(",     No of missing: "+noOfMissingBlocks);
                        Integer temp = block;
                        while(!temp.equals(currBlock))
                        {
                            temp += 1;
                            if(!presentBlocks.contains(temp)) missedBlocks.add(temp);
                        }
                    }
                    if(currBlock >= block)
                    {
                        presentBlocks.add(currBlock);
                        if(missedBlocks.contains(currBlock)) missedBlocks.remove(currBlock);
                        block = currBlock;
                    }
                }
            }
            saveFile(fileName, noOfMissingBlocks, fetchAndSavePath, timeStamp);
            System.out.println("\nTotal number of missing blocks in fileName: "+fileName+" are: " + noOfMissingBlocks+"\n\n");
        }
        System.out.println("=======================================================END=======================================================\n\n");
        System.out.println("Total missing blocks are:  " + missedBlocks.size());
        for(Integer in: presentBlocks)
        {
            if(missedBlocks.contains(in))
            {
                missedBlocks.remove(in);
            }
        }
        for(Integer in: missedBlocks)
        {
            System.out.println(in);
        }
    }


    public static void main(String[] args) throws Exception
    {
        loadDataFromFiles();
        getFilesFromServer();
        List<String> fileNames = new ArrayList<String>();
        File[] files = new File(fetchAndSavePath).listFiles();

        for (File file : files)
        {
            if (file.isFile() && file.getName().contains(".log"))
            {
                fileNames.add(file.getName());
            }
        }
        analyseBlocksData(fileNames);
    }
}