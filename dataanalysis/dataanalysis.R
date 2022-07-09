#### Load the required packages. ####
library(jsonlite)
library(dplyr)
library(tidyverse)
library(readxl)
library(ggpubr)
library(writexl)
library(car)
library(rstatix)
library(plyr)
library(DescTools)
library(rcompanion)
library(ggplot2)
options(scipen = 999)

#### ------ Step 1 : Import Data Sets ------ ####
width_PX = 1536;
width_MM = 1470;

length_L_PX = 1680;
length_L_MM = 1610; 

length_S_PX = 840;
length_S_MM = 805; 


plotColor = "#BBE8E7";
audioColor= "#ffafcc";
combinedColor = "#B6ABED"
tactileColor = "#98E3FF"

sliderDataPath = "C:/Users/kathr_/OneDrive/Desktop/HCI Master/2.Semester/IndPrak/DataAnalysis/prestudy/json/"
id_participants = c("P_1")

# Import all JSON files as data frames into a list
import_data <- function(path, ids) {
  sliderdata <- list()
  for(i in 1:length(ids)) {
    p = paste(path, ids[i], ".json", sep = "")
    sliderdata[[i]] = read_json(p, simplifyVector = TRUE)
  }
  
  return (sliderdata)
}

sliderdataList = import_data(sliderDataPath, id_participants)






#### ------ Step 2 : Quantitative Data Analysis ------ ####

# Create a dataframe with all tasks with all data fields except measurementPairs
createDataFrame <- function(sliderDataList) {
  UserId = c();
  Feedback = c();
  Length = c();
  Orientation = c();
  Phase = c(); 
  Target = c();
  Input = c();
  Error = c();
  Completiontime = c();
  
  # Add backtrackingdistance
  BacktrackingDist = c(); 
  
  # go through every user
  for(i in 1:length(sliderDataList)) {
    # go through every task
    for (j in 1:length(sliderDataList[[i]])){
      item = sliderDataList[[i]][[j]]

      UserId = c(UserId, item[[1]]);
      Feedback = c(Feedback, item[[2]]);
      Length = c(Length, item[[3]]);
      Orientation = c(Orientation, item[[4]]);
      Phase = c(Phase, item[[5]]);
      Target = c(Target, item[[6]]);
      Input = c(Input, item[[7]]);
      
      # calculate Backtracking distance
      btDist = calculateBacktrackingDistance(item);
      BacktrackingDist = c(BacktrackingDist, btDist);

      if (item[[5]] == "study"){

        Error = c(Error, item[[8]]);
        Completiontime = c(Completiontime, item[[9]]);
      } else {
        Error = c(Error, NA);
        Completiontime = c(Completiontime, item[[8]]);
      }



    }
  }
  
  
  dataframe = data.frame(UserId, Feedback, Length, Orientation, Phase, Target, Input, Error, Completiontime, BacktrackingDist)
  
  return (dataframe)
}

calculateBacktrackingDistance = function (task){
  target = task[[6]];
  pairs; 

  if (task[[5]] == "study"){
    pairs = task[[10]];
  } else {
    pairs = task[[11]];
  }

  pairsDf = data.frame(matrix(unlist(pairs), nrow=length(pairs), byrow=TRUE))
  
  userInput = pairsDf[1];
  backtrackingMax = max(userInput);
  if (backtrackingMax < target){
    backtrackingMax = target;
  }
  
  backTrackingDist = backtrackingMax - target; 
  
  pathLengthMM = 0; 
  pathLengthPX = 0; 
  stepLength = 0; 
  if (task[[3]] == "short"){
    pathLengthMM = length_S_MM; 
    pathLengthPX = length_S_PX; 

  } else {
    pathLengthMM = length_L_MM; 
    pathLengthPX = length_L_PX;

  }
  
  stepLength = pathLengthMM / 6;
  
  backTrackingMM = stepLength * backTrackingDist;
  return (backTrackingMM);

}

sliderdata = createDataFrame(sliderdataList)


# Create boxplot for backtracking
sliderdata %>% 
  ggplot(aes(y= BacktrackingDist, x=Feedback, fill=Feedback)) +
  scale_fill_manual(values = c(audioColor, combinedColor, tactileColor)) +
  geom_boxplot() +
  ggtitle("Backtracking Distance per Variant and Target") + 
  geom_jitter(width=0.05,alpha=0.15, colour  ="#031d44") +
  xlab("\nFeedback\n")+ 
  ylab("\nBacktracking Distance (mm)\n")+ 
  facet_grid(Target ~ Orientation + Length, margins=TRUE, drop = TRUE) + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1), panel.grid.major = element_line(linetype = "blank"), 
        panel.grid.minor = element_line(linetype = "solid"), 
        axis.title = element_text(family = "sans", size = 15, color = "#031d44", margin=margin(0, 40, 0, 40)), 
        axis.text = element_text(family = "mono", size = 12, color= "#031d44"),
        plot.title = element_text(family = "sans", size = 18, face="bold", color = "#031d44", margin=margin(30,0,30,0)),
        strip.background = element_rect(fill = "#031d44"),
        strip.text = element_text(face="bold", size=9, color="white"), 
        legend.text=element_text(family = "mono", size = 9, color= "#031d44"),
        legend.title=element_text(family = "mono", size = 12, color= "#031d44", margin=margin(0, 25, 0, 0)))

# Create data frames separated by study and questionnaire for quantitative data analysis

data_QUEST = subset(sliderdata, Phase == "questionnaire", select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Completiontime"));

data_STUDY = subset(sliderdata, Phase == "study", select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime"));


# Create dataframe for every variant, save in list

createDfPerVariant = function(df, phase){
  
  data_grouped = df[
    with(df, order(Feedback, Length, Orientation, Phase)),
  ]
  
  variantList <- list() 
  count = 1; 
  
 while(nrow(data_grouped) > 0){
    feedback = data_grouped$Feedback[1];
    length = data_grouped$Length[1];
    orientation = data_grouped$Orientation[1];
    
    if (phase == "questionnaire"){
      subDf = subset(df, Feedback == feedback & Length == length & Orientation == orientation & Phase == phase, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Completiontime"));
      dfLength = nrow(subDf);
      
      # Add subset as on df to list
      variantList = c(variantList, list(subDf)) 
                           
      # Delete rows of previous subset from original df
      data_grouped = data_grouped[-c(1:dfLength), ] ;
      
    } else {
      subDf = subset(df, Feedback == feedback & Length == length & Orientation == orientation & Phase == phase, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
      dfLength = nrow(subDf);
      
      # Add subset as on df to list
      variantList = c(variantList, list(subDf)) 
      
      # Delete rows of previous subset from original df
      data_grouped = data_grouped[-c(1:dfLength), ] ;

    }
    count = count +1; 


  }
  
  return(variantList);
  
  
}


variant_list_QUEST = createDfPerVariant(data_QUEST, "questionnaire");
variant_list_STUDY = createDfPerVariant(data_STUDY, "study");

# create table with statistical values (mean, deviation)
createStatisticsDf = function (list, phase){
  
  Feedback = c(); 
  Length = c(); 
  Orientation = c();
  
  CT_mean = c();
  CT_deviation = c(); 
  Error_mean = c(); 
  Error_deviation = c(); 
  n = c(); 
  
  for(i in 1:length(list)) {
    Feedback = c(Feedback, list[[i]]$Feedback[[1]]);
    Length = c(Length, list[[i]]$Length[[1]]);
    Orientation = c(Orientation, list[[i]]$Orientation[[1]]);
    
    CT_mean = c(CT_mean, round(mean(list[[i]]$Completiontime), 2));
    CT_deviation = c(CT_deviation, round(sd(list[[i]]$Completiontime),2));
    n = c(n, length(list[[i]]$Completiontime));
    
    if (phase == "study"){
      Error_mean = c(Error_mean, round(mean(list[[i]]$Error), 2));
      Error_deviation = c(Error_deviation, round(sd(list[[i]]$Error), 2));
    }
  
  }
  if (phase == "study"){
    return (data.frame(Feedback, Length, Orientation, CT_mean, CT_deviation, Error_mean, Error_deviation, n))
  } else {
    return (data.frame(Feedback, Length, Orientation, CT_mean, CT_deviation, n))
  }

  
}

statistics_STUDY = createStatisticsDf(variant_list_STUDY, "study");
statistics_QUEST = createStatisticsDf(variant_list_QUEST, "questionnaire");

# TODO: t test?







#### ------ Step 3 : Interaction Plots ------ ####

# Create list of all measurementPairs

createListOfMeasurementPairs = function (list){
  measurementPairsList = list();
  # go through every user
  for(i in 1:length(list)) {
    # go through every task
    for (j in 1:length(list[[i]])){
      item = list[[i]][[j]]
      
      if (item[[5]] == "study"){
        measurementPairsList = c(measurementPairsList, list(item[[10]])) 
      } else {
        measurementPairsList = c(measurementPairsList, list(item[[9]])) 
      }
    }
  }
  
  return (measurementPairsList);
      
}

measurementPairList = createListOfMeasurementPairs(sliderdataList);

# Save plots per variant, limtied to X rows (to define)
saveVariantPlots = function(finallist, variant, ncol, nrow, labellist, pageNr){
    pageTxt = paste("page ", pageNr, sep="");
    
    # Create Plot
    plot = ggarrange(plotlist = finallist, 
                     vjust = 1, 
                     hjust = -0.5, 
                     font.label = list(size = 32, color = "black", face = "bold", family = NULL),
                     ncol = ncol, 
                     align = "h",
                     nrow = nrow,
                     labels= paste("Target: ", labellist, sep = ""),
                     common.legend = TRUE) +
                     theme(plot.margin = margin(0.5,0.5,2,0.5, "cm")) ;
    
    plot = annotate_figure(plot, top = text_grob(variant, color = "black", size=48));
    plot = annotate_figure(plot, bottom = text_grob(pageTxt, color = "black", size=18));
    
    # Save Plot as PNG
    path = paste("C:\\Users\\kathr_\\OneDrive\\Desktop\\HCI Master\\2.Semester\\IndPrak\\DataAnalysis\\prestudy\\", variant, "_page_", pageNr, ".png", sep ="");
    
    png(file=path, width=2600, height=2000)
    print(plot);
    dev.off()
    
  }



# Create plots for measurement Pairs, for a given variant

createVariantPlot <- function(t1, t2, t3, t4, t5, t6, t7){
  
  finallist = list();
  plotlist = list();
  pageNr = 1; 
  
  tList = list(t1, t2, t3, t4, t4, t5, t6, t7); 
  
  # tList = list(t1, t2, t3);
  count = 1; 
  # go through every row of the data frames with the tasks
  for (row in 1:nrow(t1)){
    plotRow = list();
    for (t in tList){
      target = t[7];
      # get associated measurementPairs via rowname
      pairs = measurementPairList[[as.integer(rownames(t)[row])]];
      x = c();
      y = c();
      time = c();
      overshoot = c(); 
      
      # create dataframe for pairs of each variant
      for (pair in pairs){
        if (t1[4] == "horizontal"){
          x = c(x, pair[[1]]);
          y = c(y, pair[[2]]);
          
          
          
          if (pair[[1]] > target){
            overshoot = c(overshoot, "yes");
          } else {
            overshoot = c(overshoot, "no");
          }
        } else {
          x = c(x, pair[[2]]);
          y = c(y, pair[[1]]);

          if (pair[[1]] > target){
            overshoot = c(overshoot, "yes");
          } else {
            overshoot = c(overshoot, "no");
          }
        }

        
        time = c(time, pair[[3]]);
        
      }
      
      # recalculate values to mm
      if(t1[4] == "horizontal"){
        y = y * width_MM / width_PX;
      } else {
        x = x * width_MM / width_PX;
      }

      dataframe = data.frame(x,y,time, overshoot);
      plotRow = c(plotRow, list(dataframe))
      
    };
    
    ylim = 0; 
    
    # set ratio according to orientation and length
    ratio =0; 
    if (t1[4] == "vertical"){
      if (t1[3] == "short"){ratio = ratio = 5/9; 
      } else { ratio = 8/9; }
      
    } else {  if (t1[3] == "short"){  ratio = ratio = 6/4.5; 
      } else {  ratio = 6/9; }
    }
    

    
    colors = c("#80CDC1", "#EB5559");
     # create plot row

        if (t1[4][1] == "horizontal"){
        p1 = ggplot(plotRow[[1]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"), legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p2 = ggplot(plotRow[[2]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p3 = ggplot(plotRow[[3]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p4 = ggplot(plotRow[[4]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"), legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p5 = ggplot(plotRow[[5]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p6 = ggplot(plotRow[[6]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));
        p7 = ggplot(plotRow[[7]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_x_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + ylim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"), legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(y = "vertical position (mm)", x = "steps") + scale_colour_discrete(limits = c("yes", "no"));

        plotlist = list(p1, p2, p3, p4, p5, p6, p7);
        finallist = c(finallist, plotlist);
        } else {
          p1 = ggplot(plotRow[[1]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p2 = ggplot(plotRow[[2]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p3 = ggplot(plotRow[[3]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p4 = ggplot(plotRow[[4]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p5 = ggplot(plotRow[[5]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p6 = ggplot(plotRow[[6]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));
          p7 = ggplot(plotRow[[7]], aes(x=x, y=y, color = overshoot)) + geom_point(alpha = 0.3, size = 1.5) + scale_y_continuous(limits= c(0,7), breaks = seq(1, 8, 1)) + xlim(0,width_MM) + theme(legend.text = element_text(size=24, face="bold"),legend.title = element_text(size = 32, face = "bold"), aspect.ratio=ratio, axis.text=element_text(size=24), axis.title=element_text(size=32,face="bold")) + labs(x = "horizontal position (mm)", y = "steps") + scale_colour_discrete(limits = c("yes", "no"));

          plotlist = list(p1, p2, p3, p4, p5, p6, p7);
          finallist = c(finallist, plotlist);
          }
        
     print(paste ("row %% 10=", row%%10, "row== nrow(t1)", row==nrow(t1), sep =""));
     # create page for every 10 plot rows 
     nrow = 0; 
     if (row %%10 ==0){
       nrow = 10;
     } else if (row == nrow(t1)){
       nrow = row;
     }
     if (row %% 10 == 0 || row == nrow(t1)){
       variant = paste(t1[1,2], t1[1,3], t1[1,4],  sep=" ");
       ncol = 7; 
       labellist = c("1", "2", "3", "4", "5", "6", "7");
       #labellist = c("1", "2", "3");
      
       saveVariantPlots(finallist, variant, ncol, nrow, labellist, pageNr);
       pageNr = pageNr + 1; 
       finallist = c();
     }
        
  }

  
}

# !!!!!!  TODO adjust to number ot targets

orderTasksForPlotting <- function(list){
  targets = unique(data_STUDY$Target);
  targets = as.integer(targets);
  targets = sort(targets);
  sequentialTasks = list();
  
  # for every variant
  for (variant in list){
    subTask1 = subset(variant, variant$Target == 1, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask2 = subset(variant, variant$Target == 2, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask3 = subset(variant, variant$Target == 3, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask4 = subset(variant, variant$Target == 4, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask5 = subset(variant, variant$Target == 5, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask6 = subset(variant, variant$Target == 6, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    subTask7 = subset(variant, variant$Target == 7, select = c("UserId","Feedback", "Length", "Orientation", "Phase", "Input", "Target", "Error", "Completiontime")); 
    
    createVariantPlot(subTask1, subTask2, subTask3, subTask4, subTask5, subTask6, subTask7);
    }

  
  
}

orderTasksForPlotting(variant_list_STUDY)





