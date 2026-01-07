package net.minecraft.crash;

import com.google.common.collect.Lists;
import net.minecraft.util.ReportedException;
import net.minecraft.world.gen.layer.IntCache;
import net.optifine.CrashReporter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CrashReport {
    private static final Logger logger = LogManager.getLogger();
    private final String description;
    private final Throwable cause;
    private final CrashReportCategory theReportCategory = new CrashReportCategory(this, "System Details");
    private final List<CrashReportCategory> crashReportSections = Lists.newArrayList();
    private File crashReportFile;
    private boolean firstCategoryInCrashReport = true;
    private StackTraceElement[] stacktrace = new StackTraceElement[0];
    private boolean reported = false;

    public CrashReport(String descriptionIn, Throwable causeThrowable) {
        description = descriptionIn;
        cause = causeThrowable;
        populateEnvironment();
    }

    private static String getWittyComment() {
        String[] astring = new String[]{"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

        try {
            return astring[(int) (System.nanoTime() % (long) astring.length)];
        } catch (Throwable var2) {
            return "Witty comment unavailable :(";
        }
    }

    public static CrashReport makeCrashReport(Throwable causeIn, String descriptionIn) {
        CrashReport crashreport;

        if (causeIn instanceof ReportedException) {
            crashreport = ((ReportedException) causeIn).getCrashReport();
        } else {
            crashreport = new CrashReport(descriptionIn, causeIn);
        }

        return crashreport;
    }

    private void populateEnvironment() {
        theReportCategory.addCrashSectionCallable("Minecraft Version", () -> "1.8.9");
        theReportCategory.addCrashSectionCallable("Operating System", () -> System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        theReportCategory.addCrashSectionCallable("Java Version", () -> System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        theReportCategory.addCrashSectionCallable("Java VM Version", () -> System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        theReportCategory.addCrashSectionCallable("Memory", () -> {
            Runtime runtime = Runtime.getRuntime();
            long i = runtime.maxMemory();
            long j = runtime.totalMemory();
            long k = runtime.freeMemory();
            long l = i / 1024L / 1024L;
            long i1 = j / 1024L / 1024L;
            long j1 = k / 1024L / 1024L;
            return k + " bytes (" + j1 + " MB) / " + j + " bytes (" + i1 + " MB) up to " + i + " bytes (" + l + " MB)";
        });
        theReportCategory.addCrashSectionCallable("JVM Flags", () -> {
            RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
            List<String> list = runtimemxbean.getInputArguments();
            int i = 0;
            StringBuilder stringbuilder = new StringBuilder();

            for (String s : list) {
                if (s.startsWith("-X")) {
                    if (i++ > 0) {
                        stringbuilder.append(" ");
                    }

                    stringbuilder.append(s);
                }
            }

            return String.format("%d total; %s", i, stringbuilder);
        });
        theReportCategory.addCrashSectionCallable("IntCache", IntCache::getCacheSizes);
    }

    public String getDescription() {
        return description;
    }

    public Throwable getCrashCause() {
        return cause;
    }

    public void getSectionsInStringBuilder(StringBuilder builder) {
        if ((stacktrace == null || stacktrace.length <= 0) && !crashReportSections.isEmpty()) {
            stacktrace = ArrayUtils.subarray(crashReportSections.getFirst().getStackTrace(), 0, 1);
        }

        if (stacktrace != null && stacktrace.length > 0) {
            builder.append("-- Head --\n");
            builder.append("Stacktrace:\n");

            for (StackTraceElement stacktraceelement : stacktrace) {
                builder.append("\t").append("at ").append(stacktraceelement.toString());
                builder.append("\n");
            }

            builder.append("\n");
        }

        for (CrashReportCategory crashreportcategory : crashReportSections) {
            crashreportcategory.appendToStringBuilder(builder);
            builder.append("\n\n");
        }

        theReportCategory.appendToStringBuilder(builder);
    }

    public String getCauseStackTraceOrString() {
        StringWriter stringwriter = null;
        PrintWriter printwriter = null;
        Throwable throwable = cause;

        if (throwable.getMessage() == null) {
            switch (throwable) {
                case NullPointerException nullPointerException -> throwable = new NullPointerException(description);
                case StackOverflowError stackOverflowError -> throwable = new StackOverflowError(description);
                case OutOfMemoryError outOfMemoryError -> throwable = new OutOfMemoryError(description);
                default -> {
                }
            }

            throwable.setStackTrace(cause.getStackTrace());
        }

        throwable.toString();
        String s;

        try {
            stringwriter = new StringWriter();
            printwriter = new PrintWriter(stringwriter);
            throwable.printStackTrace(printwriter);
            s = stringwriter.toString();
        } finally {
            IOUtils.closeQuietly(stringwriter);
            IOUtils.closeQuietly(printwriter);
        }

        return s;
    }

    public String getCompleteReport() {
        if (!reported) {
            reported = true;
            CrashReporter.onCrashReport(this, theReportCategory);
        }

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("---- Minecraft Crash Report ----\n");
        stringbuilder.append("// ");
        stringbuilder.append(getWittyComment());
        stringbuilder.append("\n\n");
        stringbuilder.append("Time: ");
        stringbuilder.append((new SimpleDateFormat()).format(new Date()));
        stringbuilder.append("\n");
        stringbuilder.append("Description: ");
        stringbuilder.append(description);
        stringbuilder.append("\n\n");
        stringbuilder.append(getCauseStackTraceOrString());
        stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        stringbuilder.append("-".repeat(87));

        stringbuilder.append("\n\n");
        getSectionsInStringBuilder(stringbuilder);
        return stringbuilder.toString();
    }

    public File getFile() {
        return crashReportFile;
    }

    public boolean saveToFile(File toFile) {
        if (crashReportFile != null) {
            return false;
        } else {
            if (toFile.getParentFile() != null) {
                toFile.getParentFile().mkdirs();
            }

            try {
                FileWriter filewriter = new FileWriter(toFile);
                filewriter.write(getCompleteReport());
                filewriter.close();
                crashReportFile = toFile;
                return true;
            } catch (Throwable throwable) {
                logger.error("Could not save crash report to {}", toFile, throwable);
                return false;
            }
        }
    }

    public CrashReportCategory getCategory() {
        return theReportCategory;
    }

    public CrashReportCategory makeCategory(String name) {
        return makeCategoryDepth(name, 1);
    }

    public CrashReportCategory makeCategoryDepth(String categoryName, int stacktraceLength) {
        CrashReportCategory crashreportcategory = new CrashReportCategory(this, categoryName);

        if (firstCategoryInCrashReport) {
            int i = crashreportcategory.getPrunedStackTrace(stacktraceLength);
            StackTraceElement[] astacktraceelement = cause.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;

            if (j < 0) {
                System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + i + ")");
            }

            if (astacktraceelement != null && 0 <= j && j < astacktraceelement.length) {
                stacktraceelement = astacktraceelement[j];

                if (astacktraceelement.length + 1 - i < astacktraceelement.length) {
                    stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
                }
            }

            firstCategoryInCrashReport = crashreportcategory.firstTwoElementsOfStackTraceMatch(stacktraceelement, stacktraceelement1);

            if (i > 0 && !crashReportSections.isEmpty()) {
                CrashReportCategory crashreportcategory1 = crashReportSections.getLast();
                crashreportcategory1.trimStackTraceEntriesFromBottom(i);
            } else if (astacktraceelement != null && astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length) {
                stacktrace = new StackTraceElement[j];
                System.arraycopy(astacktraceelement, 0, stacktrace, 0, stacktrace.length);
            } else {
                firstCategoryInCrashReport = false;
            }
        }

        crashReportSections.add(crashreportcategory);
        return crashreportcategory;
    }
}
