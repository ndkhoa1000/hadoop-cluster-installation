# --- CẤU HÌNH ---
PROJECT_DIR = /home/hadoop_n1/wordcount
JAR_FILE = $(PROJECT_DIR)/target/wordcount-1.0.jar
CLASS_NAME = com.example.WordCount
INPUT_HDFS = /input
OUTPUT_HDFS = /output

# 📌 BIẾN QUAN TRỌNG: File input mặc định
# Dùng dấu ?= để nếu bạn không nhập gì, nó sẽ lấy "test.txt"
# Nếu bạn nhập giá trị khác, nó sẽ lấy giá trị bạn nhập.
FILE ?= test.txt

# --- CÁC LỆNH ---

# Chạy toàn bộ quy trình mặc định
all: build clean prepare run show

# 1. Build code
build:
	@echo "🚀 Đang build code..."
	mvn clean package -f $(PROJECT_DIR)/pom.xml -q

# 2. Xóa dữ liệu cũ trên HDFS
clean:
	@echo "🧹 Đang dọn dẹp HDFS..."
	-hdfs dfs -rm -r $(OUTPUT_HDFS)
	-hdfs dfs -rm -r $(INPUT_HDFS)

# 3. Đẩy file bạn chọn lên HDFS
prepare:
	@echo "📤 Đang xử lý file: $(FILE)"
	@# Kiểm tra xem file có tồn tại không trước khi upload
	@if [ ! -f "$(FILE)" ]; then \
		echo "❌ LỖI: Không tìm thấy file '$(FILE)'"; \
		exit 1; \
	fi
	hdfs dfs -mkdir -p $(INPUT_HDFS)
	hdfs dfs -put -f $(FILE) $(INPUT_HDFS)/

# 4. Chạy MapReduce
run:
	@echo "🏃 Đang chạy Hadoop..."
	hadoop jar $(JAR_FILE) $(CLASS_NAME) $(INPUT_HDFS) $(OUTPUT_HDFS)

# 5. Xem kết quả
show:
	@echo "📊 Kết quả:"
	hdfs dfs -cat $(OUTPUT_HDFS)/*