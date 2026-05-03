<script setup lang="ts">
import type { PermissionNode } from '~/types/role'
import AppCheckbox from "~/components/ui/AppCheckbox.vue";

interface Props {
  nodes: PermissionNode[]
  modelValue: number[]
}
const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: number[]]
}>()

function isChecked(id: number) {
  return props.modelValue.includes(id)
}

function isIndeterminate(node: PermissionNode): boolean {
  if (!node.children?.length) return false
  const checkedChildren = node.children.filter((c) => props.modelValue.includes(c.id))
  return checkedChildren.length > 0 && checkedChildren.length < node.children.length
}

function toggle(id: number) {
  const newValue = [...props.modelValue]
  const index = newValue.indexOf(id)
  if (index >= 0) {
    newValue.splice(index, 1)
  } else {
    newValue.push(id)
  }
  emit('update:modelValue', newValue)
}

function toggleWithChildren(node: PermissionNode) {
  const ids = getNodeIds(node)
  const allChecked = ids.every((id) => props.modelValue.includes(id))
  let newValue = [...props.modelValue]
  if (allChecked) {
    newValue = newValue.filter((id) => !ids.includes(id))
  } else {
    ids.forEach((id) => { if (!newValue.includes(id)) newValue.push(id) })
  }
  emit('update:modelValue', newValue)
}

function getNodeIds(node: PermissionNode): number[] {
  const ids = [node.id]
  if (node.children) {
    node.children.forEach((child) => ids.push(...getNodeIds(child)))
  }
  return ids
}
</script>

<template>
  <div class="space-y-1">
    <div v-for="node in nodes" :key="node.id" class="rounded-lg">
      <div class="flex items-center gap-2 py-1.5 px-2 hover:bg-surface rounded-lg">
        <AppCheckbox
          :model-value="isChecked(node.id)"
          :indeterminate="isIndeterminate(node)"
          @update:model-value="toggleWithChildren(node)"
        />
        <span class="text-sm font-medium text-text-primary">{{ node.name }}</span>
        <span class="text-xs text-text-tertiary">{{ node.code }}</span>
      </div>
      <div v-if="node.children?.length" class="ml-6 space-y-0.5">
        <div v-for="child in node.children" :key="child.id">
          <div class="flex items-center gap-2 py-1 px-2 hover:bg-surface rounded-lg">
            <AppCheckbox
              :model-value="isChecked(child.id)"
              :indeterminate="isIndeterminate(child)"
              @update:model-value="toggleWithChildren(child)"
            />
            <span class="text-sm text-text-primary">{{ child.name }}</span>
            <span class="text-xs text-text-tertiary">{{ child.code }}</span>
          </div>
          <div v-if="child.children?.length" class="ml-6 space-y-0.5">
            <div v-for="grandchild in child.children" :key="grandchild.id" class="flex items-center gap-2 py-1 px-2 hover:bg-surface rounded-lg">
              <AppCheckbox
                :model-value="isChecked(grandchild.id)"
                @update:model-value="toggle(grandchild.id)"
              />
              <span class="text-sm text-text-secondary">{{ grandchild.name }}</span>
              <span class="text-xs text-text-tertiary">{{ grandchild.apiPattern || grandchild.code }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
