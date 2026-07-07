# tests/api_test.py
# MoodBoard × MBTI 接口自动化测试脚本
# 使用方式：
# 1. 先启动后端：http://localhost:8888
# 2. 执行：python tests/api_test.py

import time
import json
import requests


BASE_URL = "http://localhost:8888/api"


class ApiTestClient:
    def __init__(self):
        self.token = ""
        self.username = f"auto_test_{int(time.time())}"
        self.password = "123456"

    def print_title(self, title):
        print("\n" + "=" * 70)
        print(title)
        print("=" * 70)

    def print_response(self, response):
        print("状态码:", response.status_code)
        try:
            print("响应体:", json.dumps(response.json(), ensure_ascii=False, indent=2))
        except Exception:
            print("响应体:", response.text)

    def get_headers(self):
        headers = {
            "Content-Type": "application/json"
        }

        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"

        return headers

    def extract_data(self, response):
        try:
            body = response.json()
        except Exception:
            return {}

        if isinstance(body, dict):
            if "data" in body and isinstance(body["data"], dict):
                return body["data"]
            return body

        return {}

    def extract_token(self, response):
        data = self.extract_data(response)

        token = (
            data.get("token")
            or data.get("accessToken")
            or data.get("jwt")
            or ""
        )

        return token

    def assert_status_ok(self, response, case_name):
        assert response.status_code == 200, f"{case_name} 失败：HTTP状态码不是200"

    def assert_has_field(self, data, field, case_name):
        assert field in data, f"{case_name} 失败：响应缺少字段 {field}"

    # 1. 注册接口
    def test_register(self):
        self.print_title("API-001 新用户注册")

        url = f"{BASE_URL}/auth/register"
        payload = {
            "username": self.username,
            "password": self.password
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "新用户注册")

        print("✅ 新用户注册通过")

    # 2. 登录接口
    def test_login(self):
        self.print_title("API-002 用户登录")

        url = f"{BASE_URL}/auth/login"
        payload = {
            "username": self.username,
            "password": self.password
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "用户登录")

        token = self.extract_token(response)

        assert token, "用户登录失败：未获取到 token"

        self.token = token

        print("✅ 用户登录通过，已获取 token")

    # 3. 错误密码登录
    def test_login_wrong_password(self):
        self.print_title("API-003 错误密码登录")

        url = f"{BASE_URL}/auth/login"
        payload = {
            "username": self.username,
            "password": "wrong_password"
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        # 有的后端错误也返回 200，有的返回 4xx，这里只要不是成功拿到 token 就算通过
        token = self.extract_token(response)

        assert not token, "错误密码登录失败：错误密码不应该返回 token"

        print("✅ 错误密码登录测试通过")

    # 4. 获取用户资料
    def test_get_profile(self):
        self.print_title("API-004 获取用户资料")

        url = f"{BASE_URL}/user/profile"

        response = requests.get(url, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "获取用户资料")

        print("✅ 获取用户资料通过")

    # 5. 保存用户资料
    def test_save_profile(self):
        self.print_title("API-005 保存用户资料")

        url = f"{BASE_URL}/user/profile"
        payload = {
            "nickname": "接口自动化测试用户",
            "occupation": "student",
            "ageRange": "18-22",
            "gender": "未透露",
            "residentPersona": "INFJ"
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "保存用户资料")

        print("✅ 保存用户资料通过")

    # 6. Memory Chat 聊天接口
    def test_memory_chat(self):
        self.print_title("API-006 Memory Chat 聊天")

        url = f"{BASE_URL}/ai/memory-chat/send"
        payload = {
            "chatType": "DAILY",
            "personaCode": "DAILY",
            "personaName": "日常树洞",
            "content": "我最近考试很焦虑，也总是拖延"
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "Memory Chat 聊天")

        data = self.extract_data(response)

        reply = (
            data.get("reply")
            or data.get("answer")
            or data.get("content")
            or ""
        )

        assert reply, "Memory Chat 聊天失败：AI回复为空"

        print("✅ Memory Chat 聊天通过")

    # 7. 获取 Memory
    def test_get_memory(self):
        self.print_title("API-007 获取用户 Memory")

        url = f"{BASE_URL}/memory/current"

        response = requests.get(url, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "获取用户 Memory")

        print("✅ 获取用户 Memory 通过")

    # 8. RAG 知识检索
    def test_knowledge_search(self):
        self.print_title("API-008 RAG 知识片段检索")

        url = f"{BASE_URL}/knowledge/search"
        params = {
            "q": "我最近考试很焦虑",
            "topK": 3
        }

        response = requests.get(url, params=params, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "RAG 知识片段检索")

        print("✅ RAG 知识片段检索通过")

    # 9. RAG 回答生成
    def test_knowledge_ask(self):
        self.print_title("API-009 RAG 回答生成")

        url = f"{BASE_URL}/knowledge/ask"
        params = {
            "q": "我最近考试很焦虑，也总是拖延，不知道怎么办",
            "topK": 3
        }

        response = requests.get(url, params=params, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "RAG 回答生成")

        data = self.extract_data(response)

        answer = (
            data.get("answer")
            or data.get("reply")
            or data.get("content")
            or ""
        )

        assert answer, "RAG 回答生成失败：answer 为空"

        print("✅ RAG 回答生成通过")

    # 10. Tool Calling 位置工具
    def test_location_tool(self):
        self.print_title("API-010 Tool Calling 位置工具")

        url = f"{BASE_URL}/ai/tools/location/reverse"
        payload = {
            "lat": 30.25,
            "lng": 120.16
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "Tool Calling 位置工具")

        print("✅ Tool Calling 位置工具通过")

    # 11. 多 Agent 圆桌
    def test_agent_roundtable(self):
        self.print_title("API-011 多 Agent 圆桌会话")

        url = f"{BASE_URL}/ai/agent/roundtable"
        payload = {
            "topic": "我最近考试很焦虑，也总是拖延，不知道今晚该怎么安排复习",
            "agents": [
                {
                    "code": "INTJ",
                    "name": "理性规划者"
                },
                {
                    "code": "INFP",
                    "name": "温柔共情者"
                }
            ]
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "多 Agent 圆桌会话")

        data = self.extract_data(response)

        assert "agents" in data, "多 Agent 圆桌失败：缺少 agents 字段"
        assert "summary" in data, "多 Agent 圆桌失败：缺少 summary 字段"

        print("✅ 多 Agent 圆桌会话通过")

    # 12. Agent 数量异常：少于 2 个
    def test_agent_roundtable_less_than_two(self):
        self.print_title("API-012 多 Agent 圆桌异常：少于 2 个 Agent")

        url = f"{BASE_URL}/ai/agent/roundtable"
        payload = {
            "topic": "我最近很焦虑",
            "agents": [
                {
                    "code": "INTJ",
                    "name": "理性规划者"
                }
            ]
        }

        response = requests.post(url, json=payload, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "多 Agent 圆桌异常测试")

        body_text = response.text

        assert "至少" in body_text or "2" in body_text, "少于 2 个 Agent 时未返回限制提示"

        print("✅ 少于 2 个 Agent 异常测试通过")

    # 13. 清空 Memory
    def test_clear_memory(self):
        self.print_title("API-013 清空用户 Memory")

        url = f"{BASE_URL}/memory/clear"

        response = requests.post(url, headers=self.get_headers())
        self.print_response(response)

        self.assert_status_ok(response, "清空用户 Memory")

        print("✅ 清空用户 Memory 通过")


def run_all_tests():
    client = ApiTestClient()

    test_cases = [
        client.test_register,
        client.test_login,
        client.test_login_wrong_password,
        client.test_get_profile,
        client.test_save_profile,
        client.test_memory_chat,
        client.test_get_memory,
        client.test_knowledge_search,
        client.test_knowledge_ask,
        client.test_location_tool,
        client.test_agent_roundtable,
        client.test_agent_roundtable_less_than_two,
        client.test_clear_memory,
    ]

    passed = 0
    failed = 0
    failed_cases = []

    print("\n开始执行 MoodBoard × MBTI 接口自动化测试")
    print(f"后端地址：{BASE_URL}")
    print(f"测试账号：{client.username}")

    for test_case in test_cases:
        try:
            test_case()
            passed += 1
        except AssertionError as e:
            failed += 1
            failed_cases.append((test_case.__name__, str(e)))
            print(f"❌ 断言失败：{e}")
        except Exception as e:
            failed += 1
            failed_cases.append((test_case.__name__, str(e)))
            print(f"❌ 执行异常：{e}")

    print("\n" + "=" * 70)
    print("接口自动化测试执行完成")
    print("=" * 70)
    print(f"总用例数：{len(test_cases)}")
    print(f"通过数：{passed}")
    print(f"失败数：{failed}")

    if failed_cases:
        print("\n失败用例：")
        for name, reason in failed_cases:
            print(f"- {name}: {reason}")
    else:
        print("\n🎉 所有接口测试通过")


if __name__ == "__main__":
    run_all_tests()