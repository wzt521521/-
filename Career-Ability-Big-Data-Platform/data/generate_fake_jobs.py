"""Generate synthetic job data matching kaggle_jobs_500.csv format."""
import csv
import random
import os
from datetime import date, timedelta

random.seed(os.urandom(16))

N = 1500  # number of rows to generate

# --- Data pools ---
TITLES = [
    "Java开发工程师", "Python开发工程师", "前端开发工程师", "后端开发工程师",
    "数据分析师", "数据工程师", "算法工程师", "AI训练师", "NLP算法工程师",
    "CV算法工程师", "推荐系统工程师", "大数据开发工程师", "运维工程师",
    "测试开发工程师", "全栈开发工程师", "iOS开发工程师", "Android开发工程师",
    "C++开发工程师", "Go开发工程师", "Rust开发工程师", "网络安全工程师",
    "区块链开发工程师", "嵌入式软件工程师", "游戏服务端开发", "产品经理",
    "UI/UX设计师", "技术总监", "架构师", "DevOps工程师", "SRE工程师",
    "数据库管理员(DBA)", "系统管理员", "技术支持工程师", "售前工程师",
    "技术文档工程师", "硬件工程师", "质量管理工程师", "机器人工程师",
    "物联网工程师", "云计算工程师", "中间件开发工程师", "风控算法工程师",
]

COMPANIES = [
    ("阿里巴巴", "10000人以上", "互联网/电商", "上市公司"),
    ("腾讯", "10000人以上", "互联网/社交", "上市公司"),
    ("字节跳动", "10000人以上", "互联网/内容", "民营"),
    ("百度", "5000-10000人", "互联网/搜索", "上市公司"),
    ("美团", "5000-10000人", "互联网/生活服务", "上市公司"),
    ("京东", "5000-10000人", "互联网/电商", "上市公司"),
    ("网易", "5000人以上", "互联网/游戏", "上市公司"),
    ("商汤科技", "1000-2000人", "人工智能", "民营"),
    ("旷视科技", "500-1000人", "人工智能", "民营"),
    ("科大讯飞", "2000-5000人", "人工智能/语音", "上市公司"),
    ("华为", "10000人以上", "通信/IT", "民营"),
    ("小米", "5000-10000人", "智能硬件", "上市公司"),
    ("OPPO", "5000-10000人", "智能硬件", "民营"),
    ("VIVO", "5000-10000人", "智能硬件", "民营"),
    ("大疆创新", "2000-5000人", "智能硬件/无人机", "民营"),
    ("海康威视", "5000-10000人", "安防/AI", "上市公司"),
    ("滴滴出行", "2000-5000人", "互联网/出行", "民营"),
    ("快手", "5000-10000人", "互联网/短视频", "上市公司"),
    ("哔哩哔哩", "2000-5000人", "互联网/视频", "上市公司"),
    ("知乎", "1000-2000人", "互联网/内容", "上市公司"),
    ("携程", "5000-10000人", "互联网/旅游", "上市公司"),
    ("拼多多", "5000-10000人", "互联网/电商", "上市公司"),
    ("蔚来汽车", "2000-5000人", "新能源汽车", "上市公司"),
    ("理想汽车", "2000-5000人", "新能源汽车", "上市公司"),
    ("小鹏汽车", "1000-2000人", "新能源汽车", "上市公司"),
    ("比亚迪", "10000人以上", "新能源汽车/电池", "上市公司"),
    ("宁德时代", "5000-10000人", "新能源/电池", "上市公司"),
    ("深信服", "2000-5000人", "网络安全", "上市公司"),
    ("奇安信", "2000-5000人", "网络安全", "上市公司"),
    ("用友网络", "2000-5000人", "企业软件", "上市公司"),
    ("金山软件", "1000-2000人", "办公软件", "上市公司"),
    ("蚂蚁集团", "5000-10000人", "金融科技", "民营"),
    ("微众银行", "1000-2000人", "金融科技", "民营"),
    ("三一重工", "5000-10000人", "制造业/重工", "上市公司"),
    ("海信集团", "5000-10000人", "家电制造", "上市公司"),
    ("格力电器", "5000-10000人", "家电制造", "上市公司"),
    ("美的集团", "5000-10000人", "家电制造", "上市公司"),
    ("京东方", "5000-10000人", "显示面板", "上市公司"),
    ("中兴通讯", "5000-10000人", "通信设备", "上市公司"),
    ("紫光展锐", "1000-2000人", "芯片设计", "民营"),
    ("长江存储", "2000-5000人", "芯片制造", "民营"),
    ("联影医疗", "1000-2000人", "医疗器械", "民营"),
    ("药明康德", "5000-10000人", "医药研发", "上市公司"),
    ("丁香园", "200-500人", "互联网医疗", "民营"),
    ("贝壳找房", "5000-10000人", "互联网/房产", "上市公司"),
    ("BOSS直聘", "1000-2000人", "互联网/招聘", "上市公司"),
    ("小红书", "2000-5000人", "互联网/社交电商", "民营"),
    ("米哈游", "2000-5000人", "游戏", "民营"),
    ("鹰角网络", "200-500人", "游戏", "民营"),
    ("莉莉丝游戏", "500-1000人", "游戏", "民营"),
]

CITIES = [
    ("北京", "北京", "一线"),
    ("上海", "上海", "一线"),
    ("广州", "广东", "一线"),
    ("深圳", "广东", "一线"),
    ("杭州", "浙江", "新一线"),
    ("成都", "四川", "新一线"),
    ("武汉", "湖北", "新一线"),
    ("西安", "陕西", "新一线"),
    ("南京", "江苏", "新一线"),
    ("重庆", "重庆", "新一线"),
    ("苏州", "江苏", "新一线"),
    ("长沙", "湖南", "新一线"),
    ("天津", "天津", "新一线"),
    ("郑州", "河南", "新一线"),
    ("东莞", "广东", "新一线"),
    ("青岛", "山东", "二线"),
    ("合肥", "安徽", "二线"),
    ("厦门", "福建", "二线"),
    ("大连", "辽宁", "二线"),
    ("福州", "福建", "二线"),
    ("济南", "山东", "二线"),
    ("佛山", "广东", "二线"),
]

EDUCATION = ["大专", "本科", "硕士", "博士", "不限"]

EXPERIENCE = ["应届", "1年以内", "1-3年", "3-5年", "5-10年", "3年及以下", "不限"]

SKILL_POOL = [
    "Java", "Python", "C++", "Go", "Rust", "Kotlin", "Swift",
    "Spring Boot", "Spring Cloud", "MyBatis", "Hibernate",
    "Django", "Flask", "FastAPI", "Node.js", "Express",
    "React", "Vue", "Angular", "TypeScript", "JavaScript",
    "Flutter", "React Native",
    "MySQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch",
    "Kafka", "RabbitMQ", "RocketMQ", "Zookeeper",
    "Docker", "Kubernetes", "Jenkins", "GitLab CI",
    "Hadoop", "Spark", "Flink", "Hive", "HBase",
    "Spark SQL", "Pandas", "NumPy", "Matplotlib", "Scikit-learn",
    "TensorFlow", "PyTorch", "OpenCV", "NLTK", "Seaborn",
    "Linux", "Shell", "Nginx", "Tomcat",
    "AWS", "阿里云", "腾讯云", "Azure",
    "Git", "SVN", "JIRA", "Confluence",
    "SQL", "NoSQL", "GraphQL", "RESTful API",
    "微服务", "DDD", "TDD", "敏捷开发",
    "Linux", "Shell", "Prometheus", "Grafana",
    "JUnit", "Selenium", "Cypress", "JMeter",
    "机器学习", "深度学习", "NLP", "计算机视觉",
]

WELFARE_POOL = [
    "五险一金", "年终奖", "带薪年假", "股票期权", "补充商业保险",
    "餐补", "交通补贴", "住房补贴", "通讯补贴",
    "年度体检", "健身房", "弹性工作制", "扁平化管理",
    "技术培训", "出国机会", "团建活动", "下午茶", "零食不限量",
    "定期调薪", "绩效奖金", "项目奖金", "结婚礼金", "生育礼金",
]

INDUSTRIES = ["互联网/电商", "互联网/社交", "互联网/内容", "互联网/游戏",
              "人工智能", "金融科技", "互联网医疗", "智能硬件",
              "新能源汽车", "新能源/电池", "通信/IT", "企业软件",
              "网络安全", "芯片设计", "在线教育", "互联网/生活服务",
              "医疗AI", "物联网", "云计算", "芯片制造",
              "制造业/重工", "家电制造", "显示面板", "医疗器械"]

COMPANY_TYPES = ["上市公司", "民营", "国企", "合资", "外商独资", "创业公司"]

SALARY_GRADE = [
    (3, 6), (5, 10), (7, 14), (10, 20), (12, 24),
    (15, 30), (18, 35), (20, 40), (25, 50), (30, 60),
    (2, 4), (4, 8), (6, 12), (8, 15), (10, 18),
    (35, 70), (40, 80),
]


def random_company():
    name, size, industry, ctype = random.choice(COMPANIES)
    return name, size, industry, ctype


def random_skills():
    k = random.randint(2, 8)
    return "|".join(random.sample(SKILL_POOL, k))


def random_welfare():
    k = random.randint(2, 6)
    return "|".join(random.sample(WELFARE_POOL, k))


def random_publish_date():
    days_ago = random.randint(0, 365)
    return (date.today() - timedelta(days=days_ago)).isoformat()


def main():
    jobs = []
    for i in range(1, N + 1):
        job_id = f"SYN-{i:06d}"
        title = random.choice(TITLES)
        company_name, company_size, industry, company_type = random_company()
        if random.random() < 0.15:  # 15% chance of different industry
            industry = random.choice(INDUSTRIES)
        if random.random() < 0.10:
            company_type = random.choice(COMPANY_TYPES)

        lo, hi = random.choice(SALARY_GRADE)
        # add some noise
        lo += random.randint(-1, 2)
        hi += random.randint(-2, 5)
        lo = max(1, lo)
        hi = max(lo + 1, hi)

        city, province, tier = random.choice(CITIES)

        # Education weighted toward 本科
        education = random.choices(EDUCATION, weights=[15, 55, 20, 5, 5], k=1)[0]
        experience = random.choice(EXPERIENCE)
        skills = random_skills()
        welfare = random_welfare()
        publish_date = random_publish_date()
        source_url = f"https://synthetic.dataset/{job_id}"

        jobs.append([
            job_id, title, company_name, company_size, industry, company_type,
            lo, hi, city, province, tier, education, experience,
            skills, welfare, publish_date, source_url
        ])

    header = [
        "jobId", "title", "companyName", "companySize", "industry", "companyType",
        "salaryMin", "salaryMax", "city", "province", "cityTier",
        "education", "experience", "skills", "welfare", "publishDate", "sourceUrl"
    ]

    out_path = os.path.join(os.path.dirname(__file__), "synthetic_jobs_1500.csv")
    with open(out_path, "w", newline="", encoding="utf-8-sig") as f:
        w = csv.writer(f)
        w.writerow(header)
        w.writerows(jobs)

    print(f"Generated {N} rows → {out_path}")


if __name__ == "__main__":
    main()
