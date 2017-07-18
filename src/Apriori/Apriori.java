package Apriori;

import Dataset.DatasetParameter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Apriori {
	public double confident = 0d;
	public double support;
	public int visitTime = 0;
	public Map<String, Integer> frequentItemMap = new TreeMap<String, Integer>();
	public TreeSet<String> candidateItemSet = new TreeSet<String>();
	public FileWriter writer;

	public Apriori(){

	}

	private Apriori(double support) {
		// this.confident = confident;
		this.support = support;
	}

	/**
	 * 
	 * @param path
	 */
	public void firstScan(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String transaction = null;
			// int totle = 0;
			while ((transaction = reader.readLine()) != null) {
				String[] items = transaction.split("\\s+");
				for (int i = 0; i < items.length; i++) {
					if (frequentItemMap.containsKey(items[i])) {
						frequentItemMap.put(items[i],
								frequentItemMap.get(items[i]) + 1);
					} else {
						frequentItemMap.put(items[i], 1);
					}
				}
				// totle++;
			}
			reader.close();
			// this.support = this.confident * totle;
			if (frequentItemMap.size() > 0) {
				for (String item : frequentItemMap.keySet()) {
					int count = frequentItemMap.get(item).intValue();

                    double dNoisyCount = this.getNoisySupport( count );
					if (dNoisyCount >= support) {
						candidateItemSet.add(item);
					//	writer.write(item + ":" + dNoisyCount + "\r\n");
                        this.writeFrequentItem( item, dNoisyCount, count );
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param path
	 */
	public void getFrequentPattern(String path) {
		int i = 2;
		while (visitTime-- > 1 && candidateItemSet != null
				&& candidateItemSet.size() > 0) {
			System.out.println("calculating the frequent " + (i++)
					+ " item set...");
			apriori_gen();
			apriori_filter(path);
		}
	}

	/**
	 */
	public void apriori_gen() {
		TreeSet<String> result = new TreeSet<String>();
		if (candidateItemSet.size() > 0) {
			for (String item1 : candidateItemSet) {
				for (String item2 : candidateItemSet) {
					if (judgeJoinable(item1, item2)) {
						int index = item2.lastIndexOf(",");
						StringBuffer tempStr = new StringBuffer();
						tempStr.append(item1 + ",");
						tempStr.append((index == -1 ? item2 : item2
								.substring(index + 1)));
						if (has_infrequent_subset(tempStr.toString()))
							result.add(tempStr.toString());
					}
				}
			}
		}
		candidateItemSet.clear();
		// �õ���ѡƵ��K+1��ʽ
		candidateItemSet.addAll(result);
	}

	/**
	 */
	public void apriori_filter(String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String transaction = null;
			frequentItemMap.clear();
			while ((transaction = reader.readLine()) != null) {
				for (String fre_pattern : candidateItemSet) {
					if (stringContain(transaction, fre_pattern)) {
						if (frequentItemMap.containsKey(fre_pattern)) {
							frequentItemMap.put(fre_pattern,
									frequentItemMap.get(fre_pattern) + 1);
						} else {
							frequentItemMap.put(fre_pattern, 1);
						}
					}
				}
			}
			reader.close();

			if (frequentItemMap.size() > 0) {
				for (String pattern : frequentItemMap.keySet()) {
					int count = frequentItemMap.get(pattern).intValue();

                    double dNoisySupport = this.getNoisySupport( count );
					if (dNoisySupport >= support) {
					//	writer.write(pattern + ":" + count + "\r\n");
                        this.writeFrequentItem( pattern, dNoisySupport, count);
					} else {
						candidateItemSet.remove(pattern);
					}
				}
			} else {
				candidateItemSet.clear();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param transaction
	 *            like %s %s %s...%s
	 * @param fre_pattern
	 *            like %s,%s,%s...%s
	 * @return
	 */
	private boolean stringContain(String transaction, String fre_pattern) {
		String[] items = fre_pattern.split(",");
		boolean containFlag = true;
		for (String item : items) {
			int index = (" " + transaction + " ").indexOf(" " + item.trim()
					+ " ");
			if (index == -1) {
				containFlag = false;
				break;
			}
		}
		return containFlag;
	}

	/**
	 * 
	 * @param item1
	 * @param item2
	 * @return
	 */
	private boolean judgeJoinable(String item1, String item2) {
		int index1 = item1.lastIndexOf(",");
		int index2 = item2.lastIndexOf(",");
		if (index1 == index2 && index1 != -1) {
			if (item1.substring(0, index1).equals(item2.substring(0, index2))
					&& item1.substring(index1 + 1).compareTo(
							item2.substring(index2 + 1)) < 0) {
				return true;
			}
		} else if (index1 == index2 && index1 == -1) {
			if (item1.compareTo(item2) < 0)
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param C
	 * @return
	 */
	private boolean has_infrequent_subset(String C) {
		boolean containFlag = true;
		String prefix = "";
		int index = C.indexOf(",");
		while (index != -1) {
			String C_sub = prefix + C.substring(index + 1);
			if (!candidateItemSet.contains(C_sub)) {
				containFlag = false;
				break;
			}
			prefix = C.substring(0, index + 1);
			index = C.indexOf(",", index + 1);
		}
		if (containFlag) {
			String str = C.substring(0, C.lastIndexOf(","));
			containFlag &= candidateItemSet.contains(str);
		}
		return containFlag;
	}

	public void initWriter(String resultPath) {
		try {
			writer = new FileWriter(resultPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeWriter() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected double getNoisySupport( int nRealSupport ){
		return nRealSupport;
	}

    protected void writeFrequentItem( String sItem, double dNoisySupport, int nRealSupport ){
        try {
            writer.write(sItem + ":" + nRealSupport + "\r\n");
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		String dataset = "BMS1";
		int dbSize = 59601; //88162;
		double th = 0.002;
		Apriori apriori = new Apriori(dbSize * th);
		String path = DatasetParameter.sDatasetPathPrefix + dataset + ".dat";
		apriori.initWriter(DatasetParameter.sDataResultPrefix + dataset +
				"\\Apriori\\Result_" + th + "_Apriori.txt");
		apriori.visitTime = 27;
		long start = System.currentTimeMillis();
		apriori.firstScan(path);
		apriori.getFrequentPattern(path);
		long end = System.currentTimeMillis();
		System.out.println("Running time: " + (end - start));
		apriori.closeWriter();
		System.out.println("Done!");
	}
}
