import requests
import time
from datetime import datetime

BASE_URL = "http://localhost:8888/api"

USERNAME = f"test_user_{int(time.time())}"
PASSWORD = "123456"


class ApiTester:
    def __init__(self):
        self.token = None
        self.results = []

    def log(self, case_id, module, name, passed, detail):
        status = "通过" if passed else "失败"
        self.results.append({
            "case_id": case_id,
            "module": module,
            "name": name,
            "status": status,
            "detail": detail
        })

        icon = "✅" if passed else "❌"
        print(f"{icon} {case_id} [{module}] {name} - {status}")
        print(f"   {detail}")

    def headers(self):
        if self.token:
            return {
                "Authorization": f"Bearer {self.token}",
                "Content-Type": "application/json"
            }
        return {
            "Content-Type": "application/json"
        }

    def get_data(self, response_json):
        """
        兼容两种返回格式：
        1. { code: 200, data: {...} }
        2. {...}
        """
        if isinstance(response_json, dict) and "data" in response_json:
            return response_json.get("data")
        return response_json

    def test_register(self):
        case_id = "AUTO-001"

        try:
            url = f"{BASE_URL}/auth/register"
            payload = {
                "username": USERNAME,
                "password": PASSWORD,
                "nickname": "自动化测试用户"
            }

            response = requests.post(url, json=payload, timeout=10)
            passed = response.status_code == 200

            self.log(
                case_id,
                "登录注册",
                "用户注册接口",
                passed,
                f"状态码：{response.status_code}，响应：{response.text[:200]}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "登录注册", "用户注册接口", False, str(e))
            return False

    def test_login(self):
        case_id = "AUTO-002"

        try:
            url = f"{BASE_URL}/auth/login"
            payload = {
                "username": USERNAME,
                "password": PASSWORD
            }

            response = requests.post(url, json=payload, timeout=10)
            json_data = response.json()
            data = self.get_data(json_data)

            token = None

            if isinstance(data, dict):
                token = data.get("token") or data.get("accessToken")

            if not token and isinstance(json_data, dict):
                token = json_data.get("token") or json_data.get("accessToken")

            passed = response.status_code == 200 and bool(token)

            if token:
                self.token = token

            self.log(
                case_id,
                "登录注册",
                "用户登录接口",
                passed,
                f"状态码：{response.status_code}，是否获取Token：{bool(token)}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "登录注册", "用户登录接口", False, str(e))
            return False

    def test_location_reverse(self):
        case_id = "AUTO-003"

        try:
            url = f"{BASE_URL}/location/reverse"
            params = {
                "lat": 30.322737,
                "lng": 120.341960
            }

            response = requests.get(url, params=params, headers=self.headers(), timeout=10)
            json_data = response.json()
            data = self.get_data(json_data)

            province = data.get("province", "") if isinstance(data, dict) else ""
            city = data.get("city", "") if isinstance(data, dict) else ""
            district = data.get("district", "") if isinstance(data, dict) else ""
            location_name = data.get("locationName", "") if isinstance(data, dict) else ""

            passed = (
                response.status_code == 200
                and bool(city)
                and bool(district)
                and location_name != "当前位置"
            )

            self.log(
                case_id,
                "定位接口",
                "高德逆地理编码接口",
                passed,
                f"状态码：{response.status_code}，province={province}，city={city}，district={district}，locationName={location_name}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "定位接口", "高德逆地理编码接口", False, str(e))
            return False

    def test_save_diary(self):
        case_id = "AUTO-004"

        try:
            url = f"{BASE_URL}/diaries"
            payload = {
                "date": datetime.now().strftime("%Y-%m-%d"),
                "emotions": ["😊", "💪"],
                "mood": "😊",
                "content": "这是一条自动化测试生成的情绪日记。",
                "keywords": "自动化测试,接口测试",
                "imageUrl": "",
                "province": "浙江省",
                "city": "杭州市",
                "district": "临平区",
                "locationName": "杭州市 临平区"
            }

            response = requests.post(url, json=payload, headers=self.headers(), timeout=10)
            passed = response.status_code == 200

            self.log(
                case_id,
                "情绪日记",
                "保存日记接口",
                passed,
                f"状态码：{response.status_code}，响应：{response.text[:200]}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "情绪日记", "保存日记接口", False, str(e))
            return False

    def test_chat(self):
        case_id = "AUTO-005"

        try:
            url = f"{BASE_URL}/ai/chat"
            payload = {
                "chatType": "DAILY",
                "persona": "DAILY",
                "content": "我今天有点累，想聊一会儿。",
                "ephemeral": False
            }

            response = requests.post(url, json=payload, headers=self.headers(), timeout=60)
            json_data = response.json()
            data = self.get_data(json_data)

            text = ""

            if isinstance(data, dict):
                text = (
                    data.get("content")
                    or data.get("reply")
                    or data.get("message")
                    or data.get("answer")
                    or ""
                )

            if not text and isinstance(json_data, dict):
                text = (
                    json_data.get("content")
                    or json_data.get("reply")
                    or json_data.get("message")
                    or json_data.get("answer")
                    or ""
                )

            passed = response.status_code == 200 and bool(text)

            self.log(
                case_id,
                "AI树洞",
                "日常聊天接口",
                passed,
                f"状态码：{response.status_code}，AI回复长度：{len(text)}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "AI树洞", "日常聊天接口", False, str(e))
            return False

    def test_persona_chat(self):
        case_id = "AUTO-006"

        try:
            url = f"{BASE_URL}/ai/chat"
            payload = {
                "chatType": "PERSONA",
                "persona": "INFP",
                "content": "我最近有点迷茫，你会怎么安慰我？",
                "ephemeral": False
            }

            response = requests.post(url, json=payload, headers=self.headers(), timeout=60)
            json_data = response.json()
            data = self.get_data(json_data)

            text = ""

            if isinstance(data, dict):
                text = (
                    data.get("content")
                    or data.get("reply")
                    or data.get("message")
                    or data.get("answer")
                    or ""
                )

            if not text and isinstance(json_data, dict):
                text = (
                    json_data.get("content")
                    or json_data.get("reply")
                    or json_data.get("message")
                    or json_data.get("answer")
                    or ""
                )

            passed = response.status_code == 200 and bool(text)

            self.log(
                case_id,
                "人格对话",
                "INFP人格聊天接口",
                passed,
                f"状态码：{response.status_code}，AI回复长度：{len(text)}"
            )

            return passed

        except Exception as e:
            self.log(case_id, "人格对话", "INFP人格聊天接口", False, str(e))
            return False

    def generate_report(self):
        total = len(self.results)
        passed = len([r for r in self.results if r["status"] == "通过"])
        failed = total - passed
        pass_rate = round(passed / total * 100, 2) if total else 0

        report = []
        report.append("# MoodBoard × MBTI 接口自动化测试报告\n")
        report.append(f"测试时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        report.append(f"测试环境：{BASE_URL}\n")
        report.append("\n## 一、测试汇总\n")
        report.append("| 总用例数 | 通过数 | 失败数 | 通过率 |")
        report.append("|---|---|---|---|")
        report.append(f"| {total} | {passed} | {failed} | {pass_rate}% |\n")

        report.append("\n## 二、测试明细\n")
        report.append("| 用例编号 | 模块 | 接口名称 | 执行结果 | 详情 |")
        report.append("|---|---|---|---|---|")

        for r in self.results:
            detail = str(r["detail"]).replace("\n", " ").replace("|", "，")
            report.append(
                f"| {r['case_id']} | {r['module']} | {r['name']} | {r['status']} | {detail} |"
            )

        with open("test-docs/06-接口自动化测试报告.md", "w", encoding="utf-8") as f:
            f.write("\n".join(report))

        print("\n测试报告已生成：test-docs/06-接口自动化测试报告.md")

    def run_all(self):
        print("开始执行 MoodBoard × MBTI 接口自动化测试...\n")

        self.test_register()
        self.test_login()

        if not self.token:
            print("\n未获取到 token，后续需要登录态的接口可能失败。\n")

        self.test_location_reverse()
        self.test_save_diary()
        self.test_chat()
        self.test_persona_chat()

        self.generate_report()


if __name__ == "__main__":
    tester = ApiTester()
    tester.run_all()